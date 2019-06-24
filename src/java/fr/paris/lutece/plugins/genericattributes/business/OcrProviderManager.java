/*
 * Copyright (c) 2002-2014, Mairie de Paris
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
package fr.paris.lutece.plugins.genericattributes.business;

import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Manages all map providers.
 */
public final class OcrProviderManager
{
    /**
     * OcrProviderManager empty constructor
     */
    private OcrProviderManager( )
    {
    }

    /**
     * Gets the ocrProvider for the provided key.
     * 
     * @param strKey
     *            the key
     * @return <code>null</code> if <code>strKey</code> is blank, the ocr provider if found, <code>null</code> otherwise.
     * @see StringUtils#isBlank(String)
     */
    public static IOcrProvider getOcrProvider( String strKey )
    {
        if ( StringUtils.isBlank( strKey ) )
        {
            return null;
        }

        for ( IOcrProvider mapProvider : getOcrProvidersList( ) )
        {
            if ( strKey.equals( mapProvider.getKey( ) ) )
            {
                return mapProvider;
            }
        }

        AppLogService.info( OcrProviderManager.class.getName( ) + " : No ocr provider found for key " + strKey );

        return null;
    }

    /**
     * Builds all available providers list
     * 
     * @return all available providers
     */
    public static List<IOcrProvider> getOcrProvidersList( )
    {
        return SpringContextService.getBeansOfType( IOcrProvider.class );
    }
}
