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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
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
     * Jacob Object to wrap A2ia component.
     */
    private Dispatch _dispatchA2iAObj;
    /**
     * clsid active x A2IA.
     */
    String           _strClsid;

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
     * @param byteImageContent
     *            image to process
     * @param strFileExtension
     *            image extension
     * @param strDocumentType
     *            document type : values allowed : Rib, TaxAssessment
     * @return Map result of OCR
     * @throws OcrException
     *             the OcrException
     *
     */
    public Map<String, String> proceed( byte[] byteImageContent, String strFileExtension, String strDocumentType ) throws OcrException
    {
        if ( StringUtils.isEmpty( _strClsid ) )
        {
            AppLogService.error( "Bad initialisation of OCR Service." );
            throw new OcrException( OcrConstants.MESSAGE_INIT_ERROR );
        }

        if ( ArrayUtils.isEmpty( byteImageContent ) || StringUtils.isEmpty( strFileExtension ) || StringUtils.isEmpty( strDocumentType ) )
        {
            throw new OcrException( I18nService.getLocalizedString( OcrConstants.MESSAGE_PARAMETER_MANDATORY, Locale.getDefault( ) ) );

        }

        Map<String,String> mapOcrServiceResults = new HashMap<>( );

        Variant variantChannelId = null;
        Variant variantRequestId = null;
        try
        {
            variantChannelId = openChannelA2ia( );
            variantRequestId = openRequestA2ia( byteImageContent, strFileExtension, strDocumentType, new Long( variantChannelId.toString( ) ) );
            // run A2IA OCR engine to get result
            AppLogService.info( "Call a2ia engine begin" );
            Variant variantResultId = Dispatch.call( _dispatchA2iAObj, "ScrGetResult", variantChannelId, variantRequestId, 60000L );
            mapOcrServiceResults = OcrResultUtils.getOcrResults( strDocumentType, _dispatchA2iAObj, variantResultId );
            AppLogService.info( "Call a2ia engine end" );

        } catch ( Exception e )
        {
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
     *
     * @return id of the channel
     */
    private Variant openChannelA2ia( )
    {

        // Init COM A2IA COM Object
        ActiveXComponent comp = new ActiveXComponent( _strClsid );
        _dispatchA2iAObj = comp.getObject( );
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
     * @return id of the request
     * @throws OcrException
     *             the OcrException
     */
    private Variant openRequestA2ia( byte[] byteImageContent, String strFileExtension, String strDocumentType, Long lChannelId ) throws OcrException
    {

        Object[] imageObjects = new Object[byteImageContent.length];
        for ( int i = 0; i < byteImageContent.length; i++ )
        {
            imageObjects[i] = new Byte( byteImageContent[i] );
        }

        // Open Tbl doc
        Variant variantTblId = Dispatch.call( _dispatchA2iAObj, "ScrOpenDocumentTable", getTblDocumentPath( strDocumentType ) );
        Variant variantDefaultDocId = Dispatch.call( _dispatchA2iAObj, "ScrGetDefaultDocument", new Long( variantTblId.toString( ) ) );

        // Following Image Parameters required to be set correctly
        Dispatch.call( _dispatchA2iAObj, OcrConstants.SET_PROPERTY_A2IA, variantDefaultDocId, "image.imageSourceType", "Memory" );
        Dispatch.call( _dispatchA2iAObj, OcrConstants.SET_PROPERTY_A2IA, variantDefaultDocId, "image.inputFormat", strFileExtension ); // format: Tiff, Bmp, or Jpeg

        // Then Set the buffer to the corresponding A2iA imageBuffer
        Dispatch.call( _dispatchA2iAObj, "ScrSetBuffer", variantDefaultDocId, "image.imageSourceTypeInfo.CaseMemory.buffer", imageObjects ); // from memory

        // Open Request
        Variant variantReqId = Dispatch.call( _dispatchA2iAObj, "ScrOpenRequest", lChannelId, new Long( variantDefaultDocId.toString( ) ) );

        return variantReqId;
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
        } else
        {
            AppLogService.error( "Bad value for document type" );
            String[] messageArgs = { strDocumentType };
            throw new OcrException( I18nService.getLocalizedString( OcrConstants.MESSAGE_DOCUMENT_TYPE_ERROR, messageArgs, Locale.getDefault( ) ) );
        }

        return strTblDocumentPath;
    }

}
