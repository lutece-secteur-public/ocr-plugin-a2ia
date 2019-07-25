/*
 * Copyright (c) 2002-2019, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.ocra2ia.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;
import com.jacob.com.Variant;

import fr.paris.lutece.plugins.ocra2ia.exception.OcrException;
import fr.paris.lutece.plugins.ocra2ia.util.OcrConstants;
import fr.paris.lutece.plugins.ocra2ia.util.OcrResultUtils;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 *
 * Ocr Service
 *
 */
public class OcrService
{


    /**
     * clsid active x A2IA.
     */
    private String   _strClsid;


    /**
     * Load DLL Jacob and _dispatchA2iAObj.
     */
    @PostConstruct
    public void init( )
    {
        try
        {
            String folder = AppPropertiesService.getProperty( OcrConstants.PROPERTY_FOLDER_DLL_JACOB );
            // Load Jacob dll
            System.load( folder + OcrConstants.JACOB_DLL64_FILE );

            // Laod A2ia ActiveX component with clsid
            _strClsid = "clsid:{" + AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_CLSID ) + "}";

        } catch ( UnsatisfiedLinkError e )
        {
            AppLogService.error( "Native code Jacob library failed to load.\n" + e );
        }

        AppLogService.info( "init OCR service done." );
    }

    /**
     * Perform OCR with A2iA.
     *
     * @param bytefileContent
     *            file to read
     * @param strFileExtension
     *            image extension
     * @param strDocumentType
     *            document type : values allowed : Rib, TaxAssessment,Identity
     * @return Map result of OCR
     * @throws OcrException
     *             the OcrException
     *
     */
    public synchronized Map<String, String> proceed( byte[] bytefileContent, String strFileExtension, String strDocumentType ) throws OcrException
    {
        /**
         * Jacob Object to wrap A2ia component.
         */
    	// Init COM A2IA COM Object
        ActiveXComponent comp = new ActiveXComponent( _strClsid );
        Dispatch _dispatchA2iAObj = comp.getObject( );
    	
        if ( StringUtils.isEmpty( _strClsid ) )
        {
            AppLogService.error( "Bad initialisation of OCR Service." );
            throw new OcrException( OcrConstants.MESSAGE_INIT_ERROR );
        }

        if ( ArrayUtils.isEmpty( bytefileContent ) || StringUtils.isEmpty( strFileExtension ) || StringUtils.isEmpty( strDocumentType ) )
        {
            throw new OcrException( I18nService.getLocalizedString( OcrConstants.MESSAGE_PARAMETER_MANDATORY, Locale.getDefault( ) ) );

        }

        ImageBean result = setValueImageExtensionAndContent( strFileExtension, bytefileContent );
        String   _strA2iaImgExtension = result.getExtension();
        byte[]   _byteImageContent    = result.getContent();

        Map<String, String> mapOcrServiceResults = new HashMap<>( );

        Variant variantChannelId = null;
        Variant variantRequestId = null;
        try
        {
            AppLogService.info( "openChannelA2ia begin" );
            variantChannelId = openChannelA2ia(_dispatchA2iAObj );
            AppLogService.info( "openChannelA2ia end" );
            variantRequestId = openRequestA2ia( _byteImageContent, _strA2iaImgExtension, strDocumentType, new Long( variantChannelId.toString( ) ),_dispatchA2iAObj );
            AppLogService.info( "openRequestA2ia end" );
            // run A2IA OCR engine to get result
            AppLogService.info( "Call a2ia engine begin" );
            Variant variantResultId = Dispatch.call( _dispatchA2iAObj, "ScrGetResult", variantChannelId, variantRequestId, 60000L );
            mapOcrServiceResults = OcrResultUtils.getOcrResults( strDocumentType, _dispatchA2iAObj, variantResultId );
            AppLogService.info( "Call a2ia engine end" );

        } catch ( Exception e )
        {
            AppLogService.error( e.getMessage( ), e );
            throw new OcrException( e.getMessage( ) );
        } finally
        {
            if ( variantRequestId != null )
            {
                Dispatch.call( _dispatchA2iAObj, "ScrCloseRequest", new Long( variantRequestId.toString( ) ) );
            }
            if ( variantChannelId != null )
            {
                Dispatch.call( _dispatchA2iAObj, "ScrCloseChannel", new Long( variantChannelId.toString( ) ) );
            }

        }

        return mapOcrServiceResults;
    }

    /**
     * Open a channel communication with A2ia.
     * @param _dispatchA2iAObj 
     *
     * @return id of the channel
     */
    private Variant openChannelA2ia(Dispatch _dispatchA2iAObj)
    {

        Dispatch.call( _dispatchA2iAObj, "ScrInit", "" );

        // Init Param
        Variant variantResChannelParamId = Dispatch.call( _dispatchA2iAObj, "ScrCreateChannelParam" );
        Dispatch.call( _dispatchA2iAObj, OcrConstants.SET_PROPERTY_A2IA, new Long( variantResChannelParamId.toString( ) ), "cpu[1].cpuServer",
                AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_SERVER_HOST, "" ) );
        Dispatch.call( _dispatchA2iAObj, OcrConstants.SET_PROPERTY_A2IA, new Long( variantResChannelParamId.toString( ) ), "cpu[1].portServer",
                AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_SERVER_PORT, "" ) );
        Dispatch.call( _dispatchA2iAObj, OcrConstants.SET_PROPERTY_A2IA, new Long( variantResChannelParamId.toString( ) ), "cpu[1].paramdir",
                AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_PARAM_DIR ) );

        // Open channel
        Variant variantResChannelId = Dispatch.call( _dispatchA2iAObj, "ScrOpenChannelExt", new Long( variantResChannelParamId.toString( ) ), 10000L );

        return variantResChannelId;
    }

    /**
     * Open a request with A2ia.
     *
     * @param byteImageContent
     *            image to process
     * @param strFileExtension
     *            image extension
     * @param strDocumentType
     *            document type
     * @param lChannelId
     *            id of the channel communication
     * @param _dispatchA2iAObj 
     * @return id of the request
     * @throws OcrException
     *             the OcrException
     */
    private Variant openRequestA2ia( byte[] byteImageContent, String strFileExtension, String strDocumentType, Long lChannelId, Dispatch _dispatchA2iAObj ) throws OcrException
    {

        // Open Tbl doc
        Variant variantTblId = Dispatch.call( _dispatchA2iAObj, "ScrOpenDocumentTable", getTblDocumentPath( strDocumentType ) );
        Variant variantDefaultDocId = Dispatch.call( _dispatchA2iAObj, "ScrGetDefaultDocument", new Long( variantTblId.toString( ) ) );

        // Following Image Parameters required to be set correctly
        Dispatch.call( _dispatchA2iAObj, OcrConstants.SET_PROPERTY_A2IA, variantDefaultDocId, "image.imageSourceType", "Memory" );
        Dispatch.call( _dispatchA2iAObj, OcrConstants.SET_PROPERTY_A2IA, variantDefaultDocId, "image.inputFormat", strFileExtension ); // format: Tiff, Bmp, or Jpeg

        // Then Set the buffer to the corresponding A2iA imageBuffer
        Dispatch.call( _dispatchA2iAObj, "ScrSetBuffer", variantDefaultDocId, "image.imageSourceTypeInfo.CaseMemory.buffer", trasformImagetoJacobOject( byteImageContent ) ); // from memory

        // Open Request
        Variant variantReqId = Dispatch.call( _dispatchA2iAObj, "ScrOpenRequest", lChannelId, new Long( variantDefaultDocId.toString( ) ) );

        return variantReqId;
    }


    /**
     * Transform each byte of the image to Jacob Variant.
     *
     * @param byteImageContent
     *            image to process
     * @return image representation in variant.
     * @throws OcrException
     *            the OcrException
     */
    private Variant trasformImagetoJacobOject( byte[] byteImageContent ) throws OcrException
    {
        AppLogService.info( "trasformImagetoJacobOject Start - byteImageContent length : " + byteImageContent.length );
        Variant variantImageObjects = new Variant( );

        SafeArray safearray = new SafeArray( Variant.VariantVariant, byteImageContent.length );
        // nNumberOfByte : number of byte processed by a single thread.
        int nNumberOfByte = AppPropertiesService.getPropertyInt( OcrConstants.PROPERTY_NUMBER_OF_BYTE_BY_THREAD, byteImageContent.length );
        // nNumberOfThread : number of thread to perform the image transformation.
        int nNumberOfThread = ( ( nNumberOfByte > 1 ) && ( nNumberOfByte < byteImageContent.length ) ) ? byteImageContent.length / nNumberOfByte : 1;

        Runnable[] tabRunnableTask = new Runnable[nNumberOfThread];
        ExecutorService executorService = Executors.newFixedThreadPool( nNumberOfThread );
        int nCurrentThreadPosition = 0;
        while ( nCurrentThreadPosition < nNumberOfThread )
        {
            int nStart = nCurrentThreadPosition * nNumberOfByte;
            int nEnd = ( nCurrentThreadPosition + 1 ) < nNumberOfThread ? (nCurrentThreadPosition + 1) * nNumberOfByte : byteImageContent.length;
            tabRunnableTask[nCurrentThreadPosition] = createRunnableTask( byteImageContent, safearray, nStart, nEnd );
            nCurrentThreadPosition++;
        }

        // process image transformation
        for ( int i = 0; i < nNumberOfThread; i++ )
        {
            executorService.submit( tabRunnableTask[i] );
        }
        executorService.shutdown( );

        boolean bExecutionComplet = false;
        try
        {
            bExecutionComplet = executorService.awaitTermination( AppPropertiesService.getPropertyInt( OcrConstants.PROPERTY_MAX_TIME_TO_PROCESS_IMAGE, 5 ), TimeUnit.SECONDS );
        } catch ( InterruptedException e )
        {
            AppLogService.error( e.getMessage( ), e );
        } finally
        {
            if ( bExecutionComplet )
            {
                variantImageObjects.putSafeArray( safearray );
                AppLogService.info( "trasformImagetoJacobOject Complet" );
            } else
            {
                throw new OcrException( I18nService.getLocalizedString( OcrConstants.MESSAGE_TIMEOUT_TRANSFORM_IMAGE, Locale.getDefault( ) ) );
            }
        }

        return variantImageObjects;
    }

    /**
     * Create a single task to transform part of the image into variant. Each task is executed in a dedicated thread.
     *
     * @param byteImageContent
     *            image to process
     * @param safearray
     *            jacob safe array
     * @param nStart
     *            first byte to process
     * @param nEnd
     *            last byte to process
     * @return a runnable task.
     */
    private Runnable createRunnableTask( byte[] byteImageContent, SafeArray safearray, int nStart, int nEnd )
    {
        return ( ) ->
        {
            for ( int i =  nStart; i < nEnd; i++ )
            {
                Variant variantByteImage = new Variant( );
                variantByteImage.putByte( byteImageContent[i] );
                safearray.setVariant( i, variantByteImage );
            }
        };
    }

    /**
     * Get the tbl document associate to document type.
     *
     * @param strDocumentType
     *            document type
     * @return path to tbl document
     * @throws OcrException
     *             the OcrException
     */
    private String getTblDocumentPath( String strDocumentType ) throws OcrException
    {
        String strTblDocumentPath = null;

        if ( strDocumentType.equalsIgnoreCase( AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_DOCUMENT_RIB ) ) )
        {
            strTblDocumentPath = AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_TBL_RIB );
        } else if ( strDocumentType.equalsIgnoreCase( AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_DOCUMENT_TAX ) ) )
        {
            strTblDocumentPath = AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_TBL_TAX );
        } else if ( strDocumentType.equalsIgnoreCase( AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_DOCUMENT_IDENTITY ) ) )
        {
            strTblDocumentPath = AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_TBL_IDENTITY );
        } else
        {
            AppLogService.error( "Bad value for document type" );
            String[] messageArgs = { strDocumentType };
            throw new OcrException( I18nService.getLocalizedString( OcrConstants.MESSAGE_DOCUMENT_TYPE_ERROR, messageArgs, Locale.getDefault( ) ) );
        }

        return strTblDocumentPath;
    }

    /**
     * Set the value for _strA2iaImgExtension and _byteImageContent
     *
     * @param bytefileContent
     *            file to read
     * @param strFileExtension
     *            image extension
     * @throws OcrException
     *             the OcrException
     */
    private ImageBean setValueImageExtensionAndContent( String strFileExtension, byte[] bytefileContent ) throws OcrException
    {
    	ImageBean result = new ImageBean();

		// control extension
        Arrays.asList( AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_EXTENSION_FILE_AUTHORIZED ).split( "," ) )
        .stream( )
        .forEach( extension ->
        {
			if ( extension.equalsIgnoreCase( strFileExtension ) && OcrConstants.EXTENSION_FILE_TIFF.equalsIgnoreCase( strFileExtension ) )
            {
                result.setContent(bytefileContent);
                result.setExtension(OcrConstants.EXTENSION_FILE_TIFF);
            } else if ( extension.equalsIgnoreCase( strFileExtension )
                    && ( OcrConstants.EXTENSION_FILE_JPEG.equalsIgnoreCase( strFileExtension ) || OcrConstants.EXTENSION_FILE_JPG.equalsIgnoreCase( strFileExtension ) ) )
            {
            	result.setContent(bytefileContent);
            	result.setExtension(OcrConstants.EXTENSION_FILE_JPEG);
            } else if ( extension.equalsIgnoreCase( strFileExtension ) && OcrConstants.EXTENSION_FILE_BMP.equalsIgnoreCase( strFileExtension ) )
            {
            	result.setContent(bytefileContent);
            	result.setExtension(OcrConstants.EXTENSION_FILE_BMP);
            } else if ( extension.equalsIgnoreCase( strFileExtension ) && OcrConstants.EXTENSION_FILE_PDF.equalsIgnoreCase( strFileExtension ) )
            {

                try
                {
                	result.setContent(transformPdfToImage( bytefileContent ));
                } catch ( OcrException | IOException e )
                {
                    AppLogService.error( e.getMessage( ) );
                }

                result.setExtension(OcrConstants.EXTENSION_FILE_JPEG);
            }
        } 
        
        );

        if ( result.getExtension()==null )
        {
            AppLogService.error( "Bad value for file extension." );
            String[] messageArgs = { strFileExtension };
            throw new OcrException( I18nService.getLocalizedString( OcrConstants.MESSAGE_FILE_EXTENSION_TYPE_ERROR, messageArgs, Locale.getDefault( ) ) );
        } else {
        	return result;
        }
    }

    /**
     * Convert pdf to Jpeg image.
     *
     * @param pdfByteContent
     *            pdf byte content
     * @return image byte content
     * @throws OcrException
     *             the OcrException
     * @throws IOException
     *             the IOException
     */
    private byte[] transformPdfToImage( byte[] pdfByteContent ) throws OcrException, IOException
    {

        AppLogService.info( "transformPdfToImage begin" );
        final ByteArrayOutputStream byteArrayos = new ByteArrayOutputStream( );
        byte[] byteImageByteContent = null;

        final PDDocument document = PDDocument.load( pdfByteContent );
        if ( document.getNumberOfPages( ) > 1 )
        {
            throw new OcrException( I18nService.getLocalizedString( OcrConstants.MESSAGE_PDF_NUMBER_PAGES_ERROR, Locale.getDefault( ) ) );
        }

        PDFRenderer pdfRenderer = new PDFRenderer( document );
        int ndpi = AppPropertiesService.getPropertyInt( OcrConstants.PROPERTY_PDF_IMAGE_QUALITY,150 );
        BufferedImage bim = pdfRenderer.renderImageWithDPI( 0, ndpi );
        ImageIOUtil.writeImage( bim, OcrConstants.EXTENSION_FILE_JPG, byteArrayos );
        byteImageByteContent = byteArrayos.toByteArray( );
        document.close( );

        AppLogService.info( "transformPdfToImage end" );

        return byteImageByteContent;

    }
    
	/**
	 * Image bean class
	 */
	private class ImageBean {
		String _strExtension;
		byte[] _byteContent;

		public String getExtension() {
			return _strExtension;
		}

		public void setExtension(String _strExtension) {
			this._strExtension = _strExtension;
		}

		public byte[] getContent() {
			return _byteContent;
		}

		public void setContent(byte[] _byteContent) {
			this._byteContent = _byteContent;
		}
	}

}
