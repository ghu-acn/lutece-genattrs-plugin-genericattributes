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

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.util.List;
import java.util.Map;

/**
 * IEntryDAO Interface
 */
public interface IEntryDAO
{
    /**
     * Insert a new record in the table.
     *
     * @param entry
     *            instance of the Entry object to insert
     * @param plugin
     *            the plugin
     * @return the id of the new entry
     */
    int insert( Entry entry, Plugin plugin );

    /**
     * Update the entry in the table
     *
     * @param entry
     *            instance of the Entry object to update
     * @param plugin
     *            the plugin
     */
    void store( Entry entry, Plugin plugin );

    /**
     * Delete a record from the table
     *
     * @param nIdEntry
     *            The identifier of the entry
     * @param plugin
     *            the plugin
     */
    void delete( int nIdEntry, Plugin plugin );

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Load the data of the entry from the table
     *
     * @param nIdEntry
     *            The identifier of the entry
     * @param plugin
     *            the plugin
     * @return the instance of the Entry
     */
    Entry load( int nIdEntry, Plugin plugin );
    
    /**
    * Load the data of the entry from the table
    *
    * @param idList
    *            The identifiers of the entries
    * @param plugin
    *            the plugin
    * @return the instance of the Entry
    */
    List<Entry> loadMultiple( List<Integer> idList, Plugin plugin );

    /**
     * Load the data of all the entry who verify the filter and returns them in a list
     *
     * @param filter
     *            the filter
     * @param plugin
     *            the plugin
     * @return the list of entry
     */
    List<Entry> selectEntryListByFilter( EntryFilter filter, Plugin plugin );

    /**
     * Return the number of entry who verify the filter
     *
     * @param filter
     *            the filter
     * @param plugin
     *            the plugin
     * @return the number of entry who verify the filter
     */
    int selectNumberEntryByFilter( EntryFilter filter, Plugin plugin );

    /**
     * Finds all the entries without any parent associated to a given resource
     * 
     * @param plugin
     *            the plugin
     * @param nIdResource
     *            the id of the resource
     * @param strResourceType
     *            the resource type
     * @return List<IEntry> the list of all the entries without parent
     */
    List<Entry> findEntriesWithoutParent( Plugin plugin, int nIdResource, String strResourceType );

    /**
     * Finds the entry (conditional question) with a given order, idDependField and the id of the resource
     * 
     * @param plugin
     *            the plugin
     * @param nOrder
     *            the order
     * @param nIdField
     *            the id of the field
     * @param nIdResource
     *            the id of the resource
     * @param strResourceType
     *            The resource type of the entry to get
     * @return List<IEntry> the list of all the entries without parent
     */
    Entry findByOrderAndIdFieldAndIdResource( Plugin plugin, int nOrder, int nIdField, int nIdResource, String strResourceType );

    /**
     * Decrements the order of all the entries (conditional questions) after the one which will be removed
     * 
     * @param plugin
     *            The plugin
     * @param nOrder
     *            the order of the entry which will be removed
     * @param nIdField
     *            the id of the field
     * @param nIdResource
     *            the id of the resource
     * @param strResourceType
     *            The resource type
     */
    void decrementOrderByOne( Plugin plugin, int nOrder, int nIdField, int nIdResource, String strResourceType );

    /**
     *
     * @param plugin
     *            The plugin
     * @param nIdForm
     *            if form
     * @return
     */
    Map<Integer, String> findEntryByForm( Plugin plugin, int nIdForm );

    /**
     *
     * @param plugin
     *            the plugin
     * @param nIdEntry
     *            id entry
     * @param nIdResponse
     *            id entry response
     * @return entry value
     */
    String getEntryValueByIdResponse( Plugin plugin, int nIdEntry, int nIdResponse );
}
