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
package fr.paris.lutece.plugins.ocra2ia.util;

/**
 *
 * Constants class for plugin ocr a2ia
 *
 */
public final class OcrConstants
{

    // i18n message
    public static final String MESSAGE_PARAMETER_MANDATORY      = "ocra2ia.message.error.parameters.mandatory";
    public static final String MESSAGE_INIT_ERROR               = "ocra2ia.message.error.init.ocr";
    public static final String MESSAGE_DOCUMENT_TYPE_ERROR      = "ocra2ia.message.error.documentType";

    // properties
    public static final String PROPERTY_FOLDER_DLL_JACOB        = "ocra2ia.jacob.dll";
    public static final String PROPERTY_A2IA_CLSID              = "ocra2ia.activex.clsid";
    public static final String PROPERTY_A2IA_SERVER_HOST        = "ocra2ia.server.host";
    public static final String PROPERTY_A2IA_SERVER_PORT        = "ocra2ia.server.port";
    public static final String PROPERTY_A2IA_PARAM_DIR          = "ocra2ia.param.dir";
    public static final String PROPERTY_A2IA_DOCUMENT_RIB       = "ocra2ia.document.rib";
    public static final String PROPERTY_A2IA_DOCUMENT_TAX       = "ocra2ia.document.tax";
    public static final String PROPERTY_A2IA_TBL_RIB            = "ocra2ia.tbl.rib";
    public static final String PROPERTY_A2IA_TBL_TAX            = "ocra2ia.tbl.tax";
    public static final String PROPERTY_RIB_RESULT              = "ocra2ia.result.rib.result";
    public static final String PROPERTY_RIB_RESULT_CODE_BANQUE  = "ocra2ia.result.rib.codebanque";
    public static final String PROPERTY_RIB_RESULT_CODE_GUICHET = "ocra2ia.result.rib.codeguichet";
    public static final String PROPERTY_RIB_RESULT_N_COMPTE     = "ocra2ia.result.rib.numerocompte";
    public static final String PROPERTY_RIB_RESULT_CLE          = "ocra2ia.result.rib.cle";
    public static final String PROPERTY_RIB_RESULT_IBAN         = "ocra2ia.result.rib.iban";
    public static final String PROPERTY_RIB_RESULT_BIC          = "ocra2ia.result.rib.bic";

    // constants
    public static final String JACOB_DLL64_FILE                 = "jacob-1.19-x64.dll";
    public static final String SET_PROPERTY_A2IA                = "SetProperty";

    // constants rib
    public static final String OUTPUT_ZONE_RIB                  = "documentTypeInfo.CaseSpecific.specificOutput.subTypeInfo.CaseRIB.RIB.result.reco";
    public static final String OUTPUT_ZONE_RIB_CODE_BANQUE      = "documentTypeInfo.CaseSpecific.specificOutput.subTypeInfo.CaseRIB.RIB.codeBanque.reco";
    public static final String OUTPUT_ZONE_RIB_CODE_GUICHET     = "documentTypeInfo.CaseSpecific.specificOutput.subTypeInfo.CaseRIB.RIB.codeGuichet.reco";
    public static final String OUTPUT_ZONE_RIB_N_COMPTE         = "documentTypeInfo.CaseSpecific.specificOutput.subTypeInfo.CaseRIB.RIB.numeroDeCompte.reco";
    public static final String OUTPUT_ZONE_RIB_CLE              = "documentTypeInfo.CaseSpecific.specificOutput.subTypeInfo.CaseRIB.RIB.cle.reco";
    public static final String OUTPUT_ZONE_RIB_IBAN             = "documentTypeInfo.CaseSpecific.specificOutput.subTypeInfo.CaseRIB.IBAN.reco";
    public static final String OUTPUT_ZONE_RIB_BIC              = "documentTypeInfo.CaseSpecific.specificOutput.subTypeInfo.CaseRIB.BIC.reco";

    /**
     * Default private constructor. Do not call
     */
    private OcrConstants( )
    {

        throw new AssertionError( );

    }

}
