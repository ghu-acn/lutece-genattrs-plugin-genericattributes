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

import fr.paris.lutece.plugins.genericattributes.util.CopyEntryEventParam;
import fr.paris.lutece.plugins.genericattributes.util.GenericAttributesUtils;
import fr.paris.lutece.portal.business.event.ResourceEvent;
import fr.paris.lutece.portal.service.event.ResourceEventManager;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.util.sql.TransactionManager;

import java.util.List;
import java.util.Map;

/**
 * This class provides instances management methods (create, find, ...) for Entry objects
 */
public final class EntryHome
{
    // Static variable pointed at the DAO instance
    private static IEntryDAO _dao = SpringContextService.getBean( "genericattributes.entryDAO" );
    private static Plugin _plugin;

    /**
     * Private constructor - this class need not be instantiated
     */
    private EntryHome( )
    {
    }

    /**
     * Creation of an instance of Entry
     * 
     * @param entry
     *            The instance of the Entry which contains the informations to store
     * @return The primary key of the new entry.
     */
    public static int create( Entry entry )
    {
        return _dao.insert( entry, getPlugin( ) );
    }

    /**
     * Copy of an instance of Entry
     * 
     * @param entry
     *            The instance of the Entry who must copy
     * @return the copied Entry
     */
    public static Entry copy( Entry entry )
    {
    	int oldId = entry.getIdEntry( );
        Entry entryCopy = (Entry) entry.clone( );
        List<Field> listField = FieldHome.getFieldListByIdEntry( entry.getIdEntry( ) );

        TransactionManager.beginTransaction( getPlugin( ) );

        try
        {
            entryCopy.setIdEntry( create( entry ) );

            for ( Field field : listField )
            {
                field = FieldHome.findByPrimaryKey( field.getIdField( ) );

                for ( Entry entryConditionnal : field.getConditionalQuestions( ) )
                {
                    entryConditionnal.setIdResource( entry.getIdResource( ) );
                    entryConditionnal.setResourceType( entry.getResourceType( ) );
                }

                field.setParentEntry( entryCopy );
                FieldHome.copy( field );
            }

            if ( entryCopy.getEntryType( ).getGroup( ) )
            {
                for ( Entry entryChild : entry.getChildren( ) )
                {
                    entryChild = EntryHome.findByPrimaryKey( entryChild.getIdEntry( ) );
                    entryChild.setParent( entryCopy );
                    entryChild.setIdResource( entryCopy.getIdResource( ) );
                    entryChild.setResourceType( entryCopy.getResourceType( ) );
                    copy( entryChild );
                }
            }
            ResourceEvent event = new ResourceEvent( );
            event.setIdResource( String.valueOf( entryCopy.getIdEntry( ) ) );
            event.setTypeResource( entry.getResourceType( ) );
            event.setParam( new CopyEntryEventParam( oldId ) );
            ResourceEventManager.fireAddedResource( event );
            
            TransactionManager.commitTransaction( getPlugin( ) );
            return entryCopy;
        }
        catch( Exception e )
        {
            TransactionManager.rollBack( getPlugin( ) );
            throw new AppException( e.getMessage( ), e );
        }
    }

    /**
     * Update of the entry which is specified in parameter
     * 
     * @param entry
     *            The instance of the Entry which contains the informations to update
     */
    public static void update( Entry entry )
    {
        _dao.store( entry, getPlugin( ) );
        ResourceEvent event = new ResourceEvent( );
        event.setIdResource( String.valueOf( entry.getIdEntry( ) ) );
        event.setTypeResource( entry.getResourceType( ) );
        ResourceEventManager.fireUpdatedResource( event );
    }

    /**
     * Remove the entry whose identifier is specified in parameter
     * 
     * @param nIdEntry
     *            The entry Id
     */
    public static void remove( int nIdEntry )
    {
        Entry entry = findByPrimaryKey( nIdEntry );

        if ( entry != null )
        {
            TransactionManager.beginTransaction( getPlugin( ) );

            try
            {
                for ( Field field : entry.getFields( ) )
                {
                    FieldHome.remove( field.getIdField( ) );
                }

                // Remove the Responses of an Entry
                removeEntryResponses( nIdEntry );

                for ( Entry entryChild : entry.getChildren( ) )
                {
                    remove( entryChild.getIdEntry( ) );
                }

                _dao.delete( nIdEntry, getPlugin( ) );
                
                ResourceEvent event = new ResourceEvent( );
                event.setIdResource( String.valueOf( nIdEntry ) );
                event.setTypeResource( entry.getResourceType( ) );
                ResourceEventManager.fireDeletedResource( event );
                
                TransactionManager.commitTransaction( getPlugin( ) );
            }
            catch( Exception e )
            {
                TransactionManager.rollBack( getPlugin( ) );
                throw new AppException( e.getMessage( ), e );
            }
        }
    }

    /**
     * Remove the Responses of an Entry
     * 
     * @param nIdEntry
     *            The identifier of the Entry to remove
     */
    private static void removeEntryResponses( int nIdEntry )
    {
        ResponseFilter responseFilter = new ResponseFilter( );
        responseFilter.setIdEntry( nIdEntry );
        List<Response> listResponse = ResponseHome.getResponseList( responseFilter );
        if ( listResponse != null && !listResponse.isEmpty( ) )
        {
            for ( Response response : listResponse )
            {
                ResponseHome.remove( response.getIdResponse( ) );
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Returns an instance of a Entry whose identifier is specified in parameter
     * 
     * @param nKey
     *            The entry primary key
     * @return an instance of Entry
     */
    public static Entry findByPrimaryKey( int nKey )
    {
        Entry entry = _dao.load( nKey, getPlugin( ) );

        if ( entry != null )
        {
            EntryFilter filter = new EntryFilter( );
            filter.setIdEntryParent( entry.getIdEntry( ) );
            entry.setChildren( getEntryList( filter ) );
            entry.setFields( FieldHome.getFieldListByIdEntry( nKey ) );
        }

        return entry;
    }
    
    /**
     * Returns a list of a Entry whose identifier is specified in parameter
     * 
     * @param idList
     *            The primary keys
     * @return an instance of Entry
     */
    public static List<Entry> findByPrimaryKeyList( List<Integer> idList )
    {
    	return _dao.loadMultiple( idList, getPlugin( ) );
    }

    /**
     * Load the data of all the entry who verify the filter and returns them in a list
     * 
     * @param filter
     *            the filter
     * @return the list of entry
     */
    public static List<Entry> getEntryList( EntryFilter filter )
    {
        return _dao.selectEntryListByFilter( filter, getPlugin( ) );
    }

    /**
     * Return the number of entry who verify the filter
     * 
     * @param filter
     *            the filter
     * @return the number of entry who verify the filter
     */
    public static int getNumberEntryByFilter( EntryFilter filter )
    {
        return _dao.selectNumberEntryByFilter( filter, getPlugin( ) );
    }

    /**
     * Finds all the entries without any parent associated with a given resource
     * 
     * @param nIdResource
     *            the id of the resource
     * @param strResourceType
     *            The resource type
     * @return List<IEntry> the list of all the entries without parent
     */
    public static List<Entry> findEntriesWithoutParent( int nIdResource, String strResourceType )
    {
        List<Entry> listEntry = _dao.findEntriesWithoutParent( getPlugin( ), nIdResource, strResourceType );

        for ( Entry entry : listEntry )
        {
            if ( entry != null )
            {
                EntryFilter filter = new EntryFilter( );
                filter.setIdEntryParent( entry.getIdEntry( ) );
                entry.setChildren( getEntryList( filter ) );
            }
        }

        return listEntry;
    }

    /**
     * Finds the entry (conditional question) with a given order, idDependField, id resource and resource type
     * 
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
    public static Entry findByOrderAndIdFieldAndIdResource( int nOrder, int nIdField, int nIdResource, String strResourceType )
    {
        return _dao.findByOrderAndIdFieldAndIdResource( getPlugin( ), nOrder, nIdField, nIdResource, strResourceType );
    }

    /**
     * Decrements the order of all the entries (conditional questions) after the one which will be removed
     * 
     * @param nOrder
     *            the order of the entry which will be removed
     * @param nIdField
     *            the id of the field
     * @param nIdResource
     *            the id of the resource
     * @param strResourceType
     *            The resource type
     */
    public static void decrementOrderByOne( int nOrder, int nIdField, int nIdResource, String strResourceType )
    {
        _dao.decrementOrderByOne( getPlugin( ), nOrder, nIdField, nIdResource, strResourceType );
    }

    /**
     * Get the generic attributes plugin
     * 
     * @return The generic attributes plugin
     */
    private static Plugin getPlugin( )
    {
        if ( _plugin == null )
        {
            _plugin = GenericAttributesUtils.getPlugin( );
        }

        return _plugin;
    }

    /**
     *
     * @param plugin
     *            the plugin
     * @param nIdForm
     *            id form
     * @return list id entry with their titles
     */
    public static Map<Integer, String> findEntryByForm( Plugin plugin, int nIdForm )
    {
        return _dao.findEntryByForm( plugin, nIdForm );
    }

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
    public static String getEntryValueByIdResponse( Plugin plugin, int nIdEntry, int nIdResponse )
    {
        return _dao.getEntryValueByIdResponse( plugin, nIdEntry, nIdResponse );
    }
}
