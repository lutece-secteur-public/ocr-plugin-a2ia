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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import fr.paris.lutece.plugins.ocra2ia.business.A2iaOutput;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 *
 * Utility class to get Ocr results
 *
 */
public final class OcrResultUtils
{

    /**
     * Default private constructor. Do not call
     */
    private OcrResultUtils( )
    {

        throw new AssertionError( );

    }

    /**
     * Get Ocr results in map.
     *
     * @param strDocumentType
     *            Document type
     *
     * @param dispatchA2iaObject
     *            A2ia Jacob wrapper
     * @param variantResultOcrId
     *            id result Ocr A2ia
     * @return Map result of OCR
     */
    public static Map<String, String> getOcrResults( String strDocumentType, Dispatch dispatchA2iaObject, Variant variantResultOcrId )
    {

        if ( strDocumentType.equalsIgnoreCase( AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_DOCUMENT_RIB ) ) )
        {
            return getRIBResult( dispatchA2iaObject, variantResultOcrId );
        }
        else
            if ( strDocumentType.equalsIgnoreCase( AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_DOCUMENT_TAX ) ) )
            {
                return getTaxAssessmentResult( dispatchA2iaObject, variantResultOcrId );
            }
            else
                if ( strDocumentType.equalsIgnoreCase( AppPropertiesService.getProperty( OcrConstants.PROPERTY_A2IA_DOCUMENT_IDENTITY ) ) )
                {
                    return getIdentityResult( dispatchA2iaObject, variantResultOcrId );
                }

        return null;
    }

    /**
     * Get Ocr results for Rib document.
     *
     * @param dispatchA2iaObject
     *            A2ia Jacob wrapper
     * @param variantResultOcrId
     *            id result Ocr A2ia
     * @return Map result of OCR
     */
    private static Map<String, String> getRIBResult( Dispatch dispatchA2iaObject, Variant variantResultOcrId )
    {

        Map<String, String> mapOcrRibResult = new HashMap<>( );

        List<A2iaOutput> listA2iaOutputRib = new ArrayList<>( );
        listA2iaOutputRib.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_RIB_RESULT ), OcrConstants.OUTPUT_ZONE_RIB,
                Variant.VariantString ) );
        listA2iaOutputRib.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_RIB_RESULT_CODE_BANQUE ),
                OcrConstants.OUTPUT_ZONE_RIB_CODE_BANQUE, Variant.VariantString ) );
        listA2iaOutputRib.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_RIB_RESULT_CODE_GUICHET ),
                OcrConstants.OUTPUT_ZONE_RIB_CODE_GUICHET, Variant.VariantString ) );
        listA2iaOutputRib.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_RIB_RESULT_N_COMPTE ),
                OcrConstants.OUTPUT_ZONE_RIB_N_COMPTE, Variant.VariantString ) );
        listA2iaOutputRib.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_RIB_RESULT_CLE ), OcrConstants.OUTPUT_ZONE_RIB_CLE,
                Variant.VariantString ) );
        listA2iaOutputRib.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_RIB_RESULT_IBAN ), OcrConstants.OUTPUT_ZONE_RIB_IBAN,
                Variant.VariantString ) );
        listA2iaOutputRib.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_RIB_RESULT_BIC ), OcrConstants.OUTPUT_ZONE_RIB_BIC,
                Variant.VariantString ) );

        listA2iaOutputRib.forEach( a2iaOutput -> {
            getA2iaOutputResult( a2iaOutput, dispatchA2iaObject, variantResultOcrId, mapOcrRibResult );

        } );

        // get Address info
        A2iaOutput a2iaOutputAddress = new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_RIB_RESULT_ADDRESS ),
                OcrConstants.OUTPUT_ZONE_RIB_ADDRESS, Variant.VariantInt );
        getA2iaOutputResultMultiLines( a2iaOutputAddress, dispatchA2iaObject, variantResultOcrId, mapOcrRibResult );

        return mapOcrRibResult;
    }

    /**
     * Get Ocr results for Tax assessment.
     *
     * @param dispatchA2iaObject
     *            A2ia Jacob wrapper
     * @param variantResultOcrId
     *            id result Ocr A2ia
     * @return Map result of OCR
     */
    private static Map<String, String> getTaxAssessmentResult( Dispatch dispatchA2iaObject, Variant variantResultOcrId )
    {

        Map<String, String> mapOcrTaxResult = new HashMap<>( );

        // get Address info
        A2iaOutput a2iaOutputAddress = new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_TAX_ASSESSMENT_RESULT_ADDRESS ),
                OcrConstants.OUTPUT_ZONE_TAX_ASSESSMENT_ADDRESS, Variant.VariantInt );
        getA2iaOutputResultMultiLines( a2iaOutputAddress, dispatchA2iaObject, variantResultOcrId, mapOcrTaxResult );

        // get established date
        A2iaOutput a2iaOutputDate = new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_TAX_ASSESSMENT_RESULT_DATE ),
                OcrConstants.OUTPUT_ZONE_TAX_ASSESSMENT_ESTABLISHED_DATE, Variant.VariantString );
        getA2iaOutputResultDate( a2iaOutputDate, dispatchA2iaObject, variantResultOcrId, mapOcrTaxResult );

        // get Tax Amount
        A2iaOutput a2iaOutputTaxAmonut = new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_TAX_ASSESSMENT_RESULT_TAX_AMOUNT ),
                OcrConstants.OUTPUT_ZONE_TAX_ASSESSMENT_TAX_AMOUNT, Variant.VariantFloat );
        getA2iaOutputResult( a2iaOutputTaxAmonut, dispatchA2iaObject, variantResultOcrId, mapOcrTaxResult );

        return mapOcrTaxResult;
    }

    /**
     * Get Ocr results for identity card document.
     *
     * @param dispatchA2iaObject
     *            A2ia Jacob wrapper
     * @param variantResultOcrId
     *            id result Ocr A2ia
     * @return Map result of OCR
     */
    private static Map<String, String> getIdentityResult( Dispatch dispatchA2iaObject, Variant variantResultOcrId )
    {

        Map<String, String> mapOcrIdentityResult = new HashMap<>( );

        List<A2iaOutput> listA2iaOutputIdentity = new ArrayList<>( );
        listA2iaOutputIdentity.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_IDENTITY_FIRST_NAME ),
                OcrConstants.OUTPUT_ZONE_IDENTITY_FIRST_NAME, Variant.VariantString ) );
        listA2iaOutputIdentity.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_IDENTITY_LAST_NAME ),
                OcrConstants.OUTPUT_ZONE_IDENTITY_LAST_NAME, Variant.VariantString ) );
        listA2iaOutputIdentity.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_IDENTITY_BIRTH_PLACE ),
                OcrConstants.OUTPUT_ZONE_IDENTITY_BIRTH_PLACE, Variant.VariantString ) );
        listA2iaOutputIdentity.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_IDENTITY_GENDER ),
                OcrConstants.OUTPUT_ZONE_IDENTITY_GENDER, Variant.VariantString ) );
        listA2iaOutputIdentity.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_IDENTITY_NATIONALITY ),
                OcrConstants.OUTPUT_ZONE_IDENTITY_NATIONALITY, Variant.VariantString ) );
        listA2iaOutputIdentity.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_IDENTITY_ID_NUMBER ),
                OcrConstants.OUTPUT_ZONE_IDENTITY_ID_NUMBER, Variant.VariantInt ) );

        listA2iaOutputIdentity.forEach( a2iaOutput -> {
            getA2iaOutputResult( a2iaOutput, dispatchA2iaObject, variantResultOcrId, mapOcrIdentityResult );

        } );

        listA2iaOutputIdentity.clear( );

        listA2iaOutputIdentity.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_IDENTITY_BIRTH_DATE ),
                OcrConstants.OUTPUT_ZONE_IDENTITY_BIRTH_DATE, Variant.VariantString ) );
        listA2iaOutputIdentity.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_IDENTITY_EXPIRATION_DATE ),
                OcrConstants.OUTPUT_ZONE_IDENTITY_EXPIRATION_DATE, Variant.VariantString ) );
        listA2iaOutputIdentity.add( new A2iaOutput( AppPropertiesService.getProperty( OcrConstants.PROPERTY_IDENTITY_ISSUE_DATE ),
                OcrConstants.OUTPUT_ZONE_IDENTITY_ISSUE_DATE, Variant.VariantString ) );

        listA2iaOutputIdentity.forEach( a2iaOutputDate -> {
            getA2iaOutputResultDate( a2iaOutputDate, dispatchA2iaObject, variantResultOcrId, mapOcrIdentityResult );
        } );

        return mapOcrIdentityResult;
    }

    /**
     * Call a2ia to get property value.
     *
     * @param a2iaOutput
     *            a2iaOutput object
     * @param dispatchA2iaObject
     *            A2ia Jacob wrapper
     * @param variantResultOcrId
     *            id result Ocr A2ia
     * @param mapResult
     *            map result of OCR
     */
    private static void getA2iaOutputResult( A2iaOutput a2iaOutput, Dispatch dispatchA2iaObject, Variant variantResultOcrId, Map<String, String> mapResult )
    {
        Variant variantResult = Dispatch
                .call( dispatchA2iaObject, OcrConstants.GET_PROPERTY_A2IA, variantResultOcrId.getInt( ), a2iaOutput.getOutputZoneName( ) );
        if ( variantResult != null )
        {
            mapResult.put( a2iaOutput.getKey( ), variantResult.changeType( a2iaOutput.getOutputZoneType( ) ).toString( ) );
        }
    }

    /**
     * Call a2ia to get property value for a multi lines output result.
     *
     * @param a2iaOutputMultiLines
     *            a2iaOutput object
     * @param dispatchA2iaObject
     *            A2ia Jacob wrapper
     * @param variantResultOcrId
     *            id result Ocr A2ia
     * @param mapResult
     *            map result of OCR
     */
    private static void getA2iaOutputResultMultiLines( A2iaOutput a2iaOutputMultiLines, Dispatch dispatchA2iaObject, Variant variantResultOcrId,
            Map<String, String> mapResult )
    {
        Variant variantLines = Dispatch.call( dispatchA2iaObject, OcrConstants.GET_PROPERTY_A2IA, variantResultOcrId.getInt( ),
                a2iaOutputMultiLines.getOutputZoneName( ) );
        if ( ( ( variantLines != null ) && !variantLines.isNull( ) ) && ( variantLines.getInt( ) > 0 ) )
        {
            StringBuilder sbAdresse = new StringBuilder( );
            for ( int i = 1; i <= variantLines.getInt( ); i++ )
            {
                Variant variantLine = Dispatch.call( dispatchA2iaObject, OcrConstants.GET_PROPERTY_A2IA, variantResultOcrId.getInt( ),
                        a2iaOutputMultiLines.getOutputZoneName( ) + "[" + i + "].wreco" );
                if ( variantLine != null )
                {
                    sbAdresse.append( variantLine.toString( ) ).append( " " );
                    Variant variantType = Dispatch.call( dispatchA2iaObject, OcrConstants.GET_PROPERTY_A2IA, variantResultOcrId.getInt( ),
                            a2iaOutputMultiLines.getOutputZoneName( ) + "[" + i + "].type" );
                    if ( OcrConstants.OUTPUT_ZONE_ADDRESS_NAME.equalsIgnoreCase( variantType.toString( ) ) )
                    {
                        mapResult.put( AppPropertiesService.getProperty( OcrConstants.PROPERTY_ADDRESS_NAME ), variantLine.toString( ) );
                    }
                    else
                        if ( OcrConstants.OUTPUT_ZONE_ADDRESS_DESTINATION.equalsIgnoreCase( variantType.toString( ) ) )
                        {
                            mapResult.put( AppPropertiesService.getProperty( OcrConstants.PROPERTY_ADDRESS_DESTINATION ), variantLine.toString( ) );
                        }
                        else
                            if ( OcrConstants.OUTPUT_ZONE_ADDRESS_PHONE_NUMBER.equalsIgnoreCase( variantType.toString( ) ) )
                            {
                                mapResult.put( AppPropertiesService.getProperty( OcrConstants.PROPERTY_ADDRESS_PHONE ), variantLine.toString( ) );
                            }
                            else
                                if ( OcrConstants.OUTPUT_ZONE_ADDRESS_CITY_ZIP.equalsIgnoreCase( variantType.toString( ) ) )
                                {
                                    mapResult.put( AppPropertiesService.getProperty( OcrConstants.PROPERTY_ADDRESS_CITYZIP ), variantLine.toString( ) );
                                }
                }

            }
            mapResult.put( a2iaOutputMultiLines.getKey( ), sbAdresse.toString( ) );
        }
    }

    /**
     * Call a2ia to get property value for an output type date.
     *
     * @param a2iaOutputDate
     *            a2iaOutput object
     * @param dispatchA2iaObject
     *            A2ia Jacob wrapper
     * @param variantResultOcrId
     *            id result Ocr A2ia
     * @param mapResult
     *            map result of OCR
     */
    private static void getA2iaOutputResultDate( A2iaOutput a2iaOutputDate, Dispatch dispatchA2iaObject, Variant variantResultOcrId,
            Map<String, String> mapResult )
    {
        String strDayOfDate = ".day";
        String strMonthOfDate = ".month";
        String strYearOfDate = ".year";
        String strDateSeparator = "/";

        Variant variantDay = Dispatch.call( dispatchA2iaObject, OcrConstants.GET_PROPERTY_A2IA, variantResultOcrId.getInt( ),
                a2iaOutputDate.getOutputZoneName( ) + strDayOfDate );
        Variant variantMonth = Dispatch.call( dispatchA2iaObject, OcrConstants.GET_PROPERTY_A2IA, variantResultOcrId.getInt( ),
                a2iaOutputDate.getOutputZoneName( ) + strMonthOfDate );
        Variant variantYear = Dispatch.call( dispatchA2iaObject, OcrConstants.GET_PROPERTY_A2IA, variantResultOcrId.getInt( ),
                a2iaOutputDate.getOutputZoneName( ) + strYearOfDate );

        if ( ( variantDay != null ) && ( variantMonth != null ) && ( variantYear != null ) )
        {
            StringBuilder sbAddressResult = new StringBuilder( );
            sbAddressResult.append( variantDay.changeType( Variant.VariantInt ).toString( ) ).append( strDateSeparator );
            sbAddressResult.append( variantMonth.changeType( Variant.VariantInt ).toString( ) ).append( strDateSeparator );
            sbAddressResult.append( variantYear.changeType( Variant.VariantInt ).toString( ) );
            mapResult.put( a2iaOutputDate.getKey( ), sbAddressResult.toString( ) );
        }

    }

}
