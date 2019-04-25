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

import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import fr.paris.lutece.plugins.ocra2ia.exception.OcrException;
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

    // i18n message
    private static final String MESSAGE_PARAMETER_MANDATORY = "ocra2ia.message.error.parameters.mandatory";
    private static final String MESSAGE_INIT_ERROR          = "ocra2ia.message.error.init.ocr";
    private static final String MESSAGE_DOCUMENT_TYPE_ERROR = "ocra2ia.message.error.documentType";

    // properties
    private static final String PROPERTY_FOLDER_DLL_JACOB   = "ocra2ia.jacob.dll";
    private static final String PROPERTY_A2IA_CLSID         = "ocra2ia.activex.clsid";
    private static final String PROPERTY_A2IA_SERVER_HOST   = "ocra2ia.server.host";
    private static final String PROPERTY_A2IA_SERVER_PORT   = "ocra2ia.server.port";
    private static final String PROPERTY_A2IA_PARAM_DIR     = "ocra2ia.param.dir";
    private static final String PROPERTY_A2IA_DOCUMENT_RIB  = "ocra2ia.document.rib";
    private static final String PROPERTY_A2IA_DOCUMENT_TAX  = "ocra2ia.document.tax";
    private static final String PROPERTY_A2IA_TBL_RIB       = "ocra2ia.tbl.rib";
    private static final String PROPERTY_A2IA_TBL_TAX       = "ocra2ia.tbl.tax";

    // constants
    private static final String JACOB_DLL64_FILE            = "jacob-1.19-x64.dll";
    private static final String SET_PROPERTY_A2IA           = "SetProperty";

    /**
     * Jacob Object to wrap A2ia component.
     */
    private Dispatch            _dispatchA2iAObj;

    /**
     * Load DLL Jacob and _dispatchA2iAObj.
     */
    @PostConstruct
    public void init( )
    {
        try
        {
            String folder = AppPropertiesService.getProperty( PROPERTY_FOLDER_DLL_JACOB );
            // Load Jacob dll
            System.load( folder + JACOB_DLL64_FILE );

            // Laod A2ia ActiveX component with clsid
            String clsid = "clsid:{" + AppPropertiesService.getProperty( PROPERTY_A2IA_CLSID ) + "}";
            ActiveXComponent comp = new ActiveXComponent( clsid );
            _dispatchA2iAObj = comp.getObject( );

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
        if ( _dispatchA2iAObj == null )
        {
            AppLogService.error( "Bad initialisation of OCR Service." );
            throw new OcrException( MESSAGE_INIT_ERROR );
        }

        if ( ArrayUtils.isEmpty( byteImageContent ) || StringUtils.isEmpty( strFileExtension ) || StringUtils.isEmpty( strDocumentType ) )
        {
            throw new OcrException( I18nService.getLocalizedString( MESSAGE_PARAMETER_MANDATORY, Locale.getDefault( ) ) );

        }

        try
        {
            Variant variantChannelId = openChannelA2ia( );
            Variant variantRequestId = openRequestA2ia( byteImageContent, strFileExtension, strDocumentType, new Long( variantChannelId.toString( ) ) );
            // run A2IA OCR engine to get result
            Variant variantResultId = Dispatch.call( _dispatchA2iAObj, "ScrGetResult", variantChannelId, variantRequestId, 60000L );

        } catch ( Exception e )
        {
            throw new OcrException( e.getMessage( ) );
        }

        return null;
    }

    /**
     * Open a channel communication with A2ia.
     *
     * @return id of the channel
     */
    private Variant openChannelA2ia( )
    {

        // Init COM A2IA COM Object
        Dispatch.call( _dispatchA2iAObj, "ScrInit", "" );

        // Init Param
        Variant variantResChannelParamId = Dispatch.call( _dispatchA2iAObj, "ScrCreateChannelParam" );
        Dispatch.call( _dispatchA2iAObj, SET_PROPERTY_A2IA, new Long( variantResChannelParamId.toString( ) ), "cpu[1].cpuServer", AppPropertiesService.getProperty( PROPERTY_A2IA_SERVER_HOST ), "" );
        Dispatch.call( _dispatchA2iAObj, SET_PROPERTY_A2IA, new Long( variantResChannelParamId.toString( ) ), "cpu[1].portServer", AppPropertiesService.getProperty( PROPERTY_A2IA_SERVER_PORT ), "" );
        Dispatch.call( _dispatchA2iAObj, SET_PROPERTY_A2IA, new Long( variantResChannelParamId.toString( ) ), "cpu[1].paramdir", AppPropertiesService.getProperty( PROPERTY_A2IA_PARAM_DIR ) );

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

        // Open Tbl doc
        Variant variantTblId = Dispatch.call( _dispatchA2iAObj, "ScrOpenDocumentTable", getTblDocumentPath( strDocumentType ) );
        Variant variantDefaultDocId = Dispatch.call( _dispatchA2iAObj, "ScrGetDefaultDocument", new Long( variantTblId.toString( ) ) );

        // Following Image Parameters required to be set correctly
        Dispatch.call( _dispatchA2iAObj, SET_PROPERTY_A2IA, variantDefaultDocId, "image.imageSourceType", "Memory" );
        Dispatch.call( _dispatchA2iAObj, SET_PROPERTY_A2IA, variantDefaultDocId, "image.inputFormat", strFileExtension ); // format: Tiff, Bmp, or Jpeg

        // Then Set the buffer to the corresponding A2iA imageBuffer
        Dispatch.call( _dispatchA2iAObj, "ScrSetBuffer", variantDefaultDocId, "image.imageSourceTypeInfo.CaseMemory.buffer", byteImageContent ); // from memory

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

        if ( strDocumentType.equalsIgnoreCase( AppPropertiesService.getProperty( PROPERTY_A2IA_DOCUMENT_RIB ) ) )
        {
            strTblDocumentPath = AppPropertiesService.getProperty( PROPERTY_A2IA_TBL_RIB );
        } else if ( strDocumentType.equalsIgnoreCase( AppPropertiesService.getProperty( PROPERTY_A2IA_DOCUMENT_TAX ) ) )
        {
            strTblDocumentPath = AppPropertiesService.getProperty( PROPERTY_A2IA_TBL_TAX );
        } else
        {
            AppLogService.error( "Bad value for document type" );
            String[] messageArgs = { strDocumentType };
            throw new OcrException( I18nService.getLocalizedString( MESSAGE_DOCUMENT_TYPE_ERROR, messageArgs, Locale.getDefault( ) ) );
        }

        return strTblDocumentPath;
    }

}
