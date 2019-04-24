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

    //i18n message
    private static final String MESSAGE_PARAMETER_MANDATORY    = "ocra2ia.message.error.parameters.mandatory";
    private static final String MESSAGE_INIT_ERROR             = "ocra2ia.message.error.init.ocr";

    // properties
    private static final String PROPERTY_FOLDER_DLL_JACOB      = "ocra2ia.jacob.dll";
    private static final String PROPERTY_A2IA_CLSID            = "ocra2ia.activex.clsid";

    // constants
    private static final String JACOB_DLL64_FILE               = "jacob-1.19-x64.dll";

    /**
     * Jacob Object to wrap A2ia component.
     */
    private Dispatch            a2iAObj;

    @PostConstruct
    public void init( )
    {
        try
        {
            String folder =  AppPropertiesService.getProperty( PROPERTY_FOLDER_DLL_JACOB );
            // Load Jacob dll
            System.load( folder+JACOB_DLL64_FILE );

            // Laod A2ia ActiveX component with clsid
            String clsid = "clsid:{" + AppPropertiesService.getProperty( PROPERTY_A2IA_CLSID ) + "}";
            ActiveXComponent comp = new ActiveXComponent( clsid );
            a2iAObj = comp.getObject( );

        } catch ( UnsatisfiedLinkError e )
        {
            AppLogService.error( "Native code Jacob library failed to load.\n" + e );
        }

        AppLogService.info( "init OCR service done." );
    }


    /**
     * Perform OCR with A2iA.
     *
     * @param imageContent
     *           image to process
     * @param fileExtension
     *           image extension
     * @param documentType
     *            document type
     * @return  Map result of OCR
     * @throws OcrException
     *
     */
    public Map<String, String> proceed( byte[] imageContent, String fileExtension, String documentType ) throws OcrException
    {
        if (a2iAObj == null ) {
            AppLogService.error( "Bad initialisation of OCR Service.");
            throw new OcrException( MESSAGE_INIT_ERROR );
        }

        if ( ArrayUtils.isEmpty( imageContent )  || StringUtils.isEmpty( fileExtension ) || StringUtils.isEmpty( documentType ) )
        {
            throw new OcrException( I18nService.getLocalizedString( MESSAGE_PARAMETER_MANDATORY , Locale.getDefault( )) ) ;

        }

        return null;
    }

}
