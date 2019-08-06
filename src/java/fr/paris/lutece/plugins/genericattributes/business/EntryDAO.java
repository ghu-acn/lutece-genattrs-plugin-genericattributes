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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

/**
 * This class provides Data Access methods for Entry objects
 */
public final class EntryDAO implements IEntryDAO
{
    // Constants
    private static final String SQL_QUERY_NEW_PK = "SELECT MAX( id_entry ) FROM genatt_entry";
    private static final String SQL_QUERY_SELECT_ENTRY_ATTRIBUTES = "SELECT ent.id_type,typ.title,typ.is_group,typ.is_comment,typ.class_name,typ.is_mylutece_user,typ.icon_name,"
            + "ent.id_entry,ent.id_resource,ent.resource_type,ent.id_parent,ent.code,ent.title,ent.help_message, ent.comment,ent.mandatory,ent.fields_in_line,"
            + "ent.pos,ent.id_field_depend,ent.confirm_field,ent.confirm_field_title,ent.field_unique, ent.map_provider, ent.css_class, ent.pos_conditional, ent.error_message, "
            + "ent.num_row, ent.num_column, ent.is_role_associated,ent.is_only_display_back, ent.is_editable_back , ent.is_indexed, ent.is_shown_in_completeness "
            + "FROM genatt_entry ent,genatt_entry_type typ WHERE ent.id_type=typ.id_type ";
    private static final String SQL_QUERY_FIND_BY_PRIMARY_KEY = SQL_QUERY_SELECT_ENTRY_ATTRIBUTES + " AND ent.id_entry = ? ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO genatt_entry ( id_entry,id_resource,resource_type,id_type,id_parent,code,title,help_message, comment,mandatory,fields_in_line,"
            + "pos,id_field_depend,confirm_field,confirm_field_title,field_unique,map_provider,css_class, pos_conditional, error_message, num_row, num_column, is_role_associated, is_only_display_back, is_editable_back, is_indexed, is_shown_in_completeness ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SQL_QUERY_DELETE = "DELETE FROM genatt_entry WHERE id_entry = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE genatt_entry SET id_entry=?,id_resource=?,resource_type=?,id_type=?,id_parent=?,code=?,title=?,help_message=?,"
            + "comment=?,mandatory=?, fields_in_line=?,pos=?,id_field_depend=?,confirm_field=?,confirm_field_title=?,field_unique=?,map_provider=?,css_class=?, pos_conditional=?, "
            + "error_message=?, num_row = ?, num_column = ?, is_role_associated = ?, is_only_display_back = ?, is_editable_back = ?, is_indexed = ?, is_shown_in_completeness=? WHERE id_entry=?";
    private static final String SQL_QUERY_SELECT_ENTRY_BY_FILTER = SQL_QUERY_SELECT_ENTRY_ATTRIBUTES;
    private static final String SQL_QUERY_SELECT_NUMBER_ENTRY_BY_FILTER = "SELECT COUNT(ent.id_entry) "
            + "FROM genatt_entry ent,genatt_entry_type typ WHERE ent.id_type=typ.id_type ";
    private static final String SQL_QUERY_NEW_POSITION = "SELECT MAX(pos) " + "FROM genatt_entry WHERE id_resource=? AND resource_type=?";
    private static final String SQL_QUERY_NEW_POSITION_CONDITIONAL_QUESTION = "SELECT MAX(pos_conditional) FROM genatt_entry WHERE id_field_depend=?";
    private static final String SQL_QUERY_NUMBER_CONDITIONAL_QUESTION = "SELECT COUNT(e2.id_entry) "
            + "FROM genatt_entry e1,genatt_field f1,genatt_entry e2 WHERE e1.id_entry=? AND e1.id_entry=f1.id_entry and e2.id_field_depend=f1.id_field ";
    private static final String SQL_FILTER_ID_RESOURCE = " AND ent.id_resource = ? ";
    private static final String SQL_FILTER_RESOURCE_TYPE = " AND ent.resource_type = ? ";
    private static final String SQL_FILTER_ID_PARENT = " AND ent.id_parent = ? ";
    private static final String SQL_FILTER_ID_PARENT_IS_NULL = " AND ent.id_parent IS NULL ";
    private static final String SQL_FILTER_IS_GROUP = " AND typ.is_group = ? ";
    private static final String SQL_FILTER_IS_COMMENT = " AND typ.is_comment = ? ";
    private static final String SQL_FILTER_ID_FIELD_DEPEND = " AND ent.id_field_depend = ? ";
    private static final String SQL_FILTER_ID_FIELD_DEPEND_IS_NULL = " AND ent.id_field_depend IS NULL ";
    private static final String SQL_FILTER_ID_TYPE = " AND ent.id_type = ? ";
    private static final String SQL_FILTER_IS_ONLY_DISPLAY_IN_BACK = " AND ent.is_only_display_back = ? ";
    private static final String SQL_FILTER_IS_EDITABLE_BACK = " AND ent.is_editable_back = ? ";
    private static final String SQL_FILTER_IS_INDEXED = " AND ent.is_indexed = ? ";
    private static final String SQL_FILTER_IS_SHOWN_COMPLETNESS = " AND ent.is_shown_in_completeness = ? ";
    private static final String SQL_ORDER_BY_POSITION = " ORDER BY ent.pos, ent.pos_conditional ";
    private static final String SQL_GROUP_BY_POSITION = " GROUP BY ent.pos, ent.pos_conditional ";
    private static final String SQL_GROUP_BY_ENTRY_ENTRY_TYPE = "GROUP BY ent.id_type,typ.title,typ.is_group,typ.is_comment,typ.class_name,typ.is_mylutece_user,"
            + "ent.id_entry,ent.id_resource,ent.resource_type,ent.id_parent,ent.title,ent.help_message,ent.comment,ent.mandatory,ent.fields_in_line,"
            + "ent.pos,ent.pos_conditional,ent.id_field_depend,ent.confirm_field,ent.confirm_field_title,ent.field_unique,ent.map_provider,ent.css_class,ent.error_message ";
    private static final String SQL_QUERY_ENTRIES_PARENT_NULL = SQL_QUERY_SELECT_ENTRY_ATTRIBUTES
            + " AND id_parent IS NULL AND id_resource=? AND resource_type = ?" + SQL_FILTER_ID_FIELD_DEPEND_IS_NULL + " ORDER BY ent.pos";
    private static final String SQL_QUERY_ENTRY_CONDITIONAL_WITH_ORDER_BY_FIELD = SQL_QUERY_SELECT_ENTRY_ATTRIBUTES
            + " AND pos_conditional = ?  AND ent.id_field_depend = ? AND id_resource=? ";
    private static final String SQL_QUERY_DECREMENT_ORDER_CONDITIONAL = "UPDATE genatt_entry SET pos_conditional = pos_conditional - 1 WHERE pos_conditional > ? AND id_field_depend=? AND id_resource=? AND resource_type=? ";
    private static final int CONSTANT_ZERO = 0;
    private static final String SQL_QUERY_SELECT_ENTRY_BY_FORM = "SELECT id_entry, title FROM genatt_entry WHERE id_resource = ? AND title IS NOT NULL ORDER BY id_entry ";
    private static final String SQL_QUERY_SELECT_ENTRY_VALUE = "SELECT title FROM genatt_response INNER JOIN genatt_field ON genatt_response.id_field = genatt_field.id_field "
            + "	WHERE genatt_response.id_entry = ? AND genatt_response.id_response = ? AND title IS NOT NULL  ORDER BY genatt_response.id_entry ";
    private static final String SQL_QUERY_FIND_BY_PRIMARY_KEY_LIST = SQL_QUERY_SELECT_ENTRY_ATTRIBUTES + " AND ent.id_entry IN ( ";

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int insert( Entry entry, Plugin plugin )
    {
        entry.setIdEntry( newPrimaryKey( plugin ) );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );

        daoUtil.setInt( 1, entry.getIdEntry( ) );
        daoUtil.setInt( 2, entry.getIdResource( ) );
        daoUtil.setString( 3, entry.getResourceType( ) );
        daoUtil.setInt( 4, entry.getEntryType( ).getIdType( ) );

        if ( entry.getParent( ) != null )
        {
            daoUtil.setInt( 5, entry.getParent( ).getIdEntry( ) );
        }
        else
        {
            daoUtil.setIntNull( 5 );
        }

        daoUtil.setString( 6, entry.getCode( ) );
        daoUtil.setString( 7, trimEntryTitle( entry ) );
        daoUtil.setString( 8, entry.getHelpMessage( ) );
        daoUtil.setString( 9, entry.getComment( ) );
        daoUtil.setBoolean( 10, entry.isMandatory( ) );
        daoUtil.setBoolean( 11, entry.isFieldInLine( ) );

        daoUtil.setInt( 12, newPosition( entry, plugin ) );

        if ( entry.getFieldDepend( ) != null )
        {
            daoUtil.setInt( 13, entry.getFieldDepend( ).getIdField( ) );
        }
        else
        {
            daoUtil.setIntNull( 13 );
        }

        daoUtil.setBoolean( 14, entry.isConfirmField( ) );
        daoUtil.setString( 15, entry.getConfirmFieldTitle( ) );
        daoUtil.setBoolean( 16, entry.isUnique( ) );

        String strMapProviderKey = ( entry.getMapProvider( ) == null ) ? StringUtils.EMPTY : entry.getMapProvider( ).getKey( );

        daoUtil.setString( 17, strMapProviderKey );
        daoUtil.setString( 18, ( entry.getCSSClass( ) == null ) ? StringUtils.EMPTY : entry.getCSSClass( ) );
        daoUtil.setInt( 19, newPositionConditional( entry, plugin ) );
        daoUtil.setString( 20, entry.getErrorMessage( ) );
        daoUtil.setInt( 21, entry.getNumberRow( ) );
        daoUtil.setInt( 22, entry.getNumberColumn( ) );
        daoUtil.setBoolean( 23, entry.isRoleAssociated( ) );
        daoUtil.setBoolean( 24, entry.isOnlyDisplayInBack( ) );
        daoUtil.setBoolean( 25, entry.isEditableBack( ) );
        daoUtil.setBoolean( 26, entry.isIndexed( ) );
        daoUtil.setBoolean( 27, entry.isShownInCompleteness( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );

        return entry.getIdEntry( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry load( int nId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_BY_PRIMARY_KEY, plugin );
        daoUtil.setInt( 1, nId );
        daoUtil.executeQuery( );

        Entry entry = null;

        if ( daoUtil.next( ) )
        {
            entry = getEntryValues( daoUtil );
        }

        daoUtil.free( );

        if ( entry != null )
        {
            entry.setNumberConditionalQuestion( numberConditionalQuestion( entry.getIdEntry( ), plugin ) );
        }

        return entry;
    }
    
    @Override
    public List<Entry> loadMultiple( List<Integer> idList, Plugin plugin )
    {
    	List<Entry> list = new ArrayList<>( );
    	String query = SQL_QUERY_FIND_BY_PRIMARY_KEY_LIST + idList.stream( ).distinct( ).map( i -> "?" ).collect( Collectors.joining( "," ) ) + " )";
    	
    	try (  DAOUtil daoUtil = new DAOUtil( query, plugin ) )
    	{
    		for ( int i = 0; i < idList.size( ); i++ )
    		{
    			daoUtil.setInt( i + 1 , idList.get( i ) );
    		}
    		daoUtil.executeQuery( );
    		
    		while ( daoUtil.next( ) )
            {
    			list.add( getEntryValues( daoUtil ) );
            }
    	}
    	return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nIdEntry, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nIdEntry );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( Entry entry, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );

        int nIndex = 1;
        daoUtil.setInt( nIndex++, entry.getIdEntry( ) );
        daoUtil.setInt( nIndex++, entry.getIdResource( ) );
        daoUtil.setString( nIndex++, entry.getResourceType( ) );
        daoUtil.setInt( nIndex++, entry.getEntryType( ).getIdType( ) );

        if ( entry.getParent( ) != null )
        {
            daoUtil.setInt( nIndex++, entry.getParent( ).getIdEntry( ) );
        }
        else
        {
            daoUtil.setIntNull( nIndex++ );
        }

        daoUtil.setString( nIndex++, entry.getCode( ) );
        daoUtil.setString( nIndex++, trimEntryTitle( entry ) );
        daoUtil.setString( nIndex++, entry.getHelpMessage( ) );
        daoUtil.setString( nIndex++, entry.getComment( ) );
        daoUtil.setBoolean( nIndex++, entry.isMandatory( ) );
        daoUtil.setBoolean( nIndex++, entry.isFieldInLine( ) );

        if ( entry.getFieldDepend( ) == null )
        {
            daoUtil.setInt( nIndex++, entry.getPosition( ) );
        }
        else
        {
            daoUtil.setInt( nIndex++, CONSTANT_ZERO );
        }

        if ( entry.getFieldDepend( ) != null )
        {
            daoUtil.setInt( nIndex++, entry.getFieldDepend( ).getIdField( ) );
        }
        else
        {
            daoUtil.setIntNull( nIndex++ );
        }

        daoUtil.setBoolean( nIndex++, entry.isConfirmField( ) );
        daoUtil.setString( nIndex++, entry.getConfirmFieldTitle( ) );
        daoUtil.setBoolean( nIndex++, entry.isUnique( ) );

        String strMapProviderKey = ( entry.getMapProvider( ) == null ) ? StringUtils.EMPTY : entry.getMapProvider( ).getKey( );

        daoUtil.setString( nIndex++, strMapProviderKey );
        daoUtil.setString( nIndex++, ( entry.getCSSClass( ) == null ) ? StringUtils.EMPTY : entry.getCSSClass( ) );

        if ( entry.getFieldDepend( ) != null )
        {
            daoUtil.setInt( nIndex++, entry.getPosition( ) );
        }
        else
        {
            daoUtil.setInt( nIndex++, CONSTANT_ZERO );
        }

        daoUtil.setString( nIndex++, entry.getErrorMessage( ) );
        daoUtil.setInt( nIndex++, entry.getNumberRow( ) );
        daoUtil.setInt( nIndex++, entry.getNumberColumn( ) );
        daoUtil.setBoolean( nIndex++, entry.isRoleAssociated( ) );
        daoUtil.setBoolean( nIndex++, entry.isOnlyDisplayInBack( ) );
        daoUtil.setBoolean( nIndex++, entry.isEditableBack( ) );
        daoUtil.setBoolean( nIndex++, entry.isIndexed( ) );
        daoUtil.setBoolean( nIndex++, entry.isShownInCompleteness( ) );

        daoUtil.setInt( nIndex++, entry.getIdEntry( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> selectEntryListByFilter( EntryFilter filter, Plugin plugin )
    {
        List<Entry> entryList = new ArrayList<Entry>( );

        StringBuilder sbSQL = new StringBuilder( SQL_QUERY_SELECT_ENTRY_BY_FILTER );

        sbSQL.append( ( filter.containsIdResource( ) ) ? SQL_FILTER_ID_RESOURCE : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsResourceType( ) ) ? SQL_FILTER_RESOURCE_TYPE : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIdEntryParent( ) ) ? SQL_FILTER_ID_PARENT : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsEntryParentNull( ) ) ? SQL_FILTER_ID_PARENT_IS_NULL : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIdIsGroup( ) ) ? SQL_FILTER_IS_GROUP : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIdField( ) ) ? SQL_FILTER_ID_FIELD_DEPEND : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsFieldDependNull( ) ) ? SQL_FILTER_ID_FIELD_DEPEND_IS_NULL : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIdEntryType( ) ) ? SQL_FILTER_ID_TYPE : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIdIsComment( ) ) ? SQL_FILTER_IS_COMMENT : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIsOnlyDisplayInBack( ) ) ? SQL_FILTER_IS_ONLY_DISPLAY_IN_BACK : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIsEditableBack( ) ) ? SQL_FILTER_IS_EDITABLE_BACK : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIsIndexed( ) ) ? SQL_FILTER_IS_INDEXED : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIsShownInCompleteness( ) ) ? SQL_FILTER_IS_SHOWN_COMPLETNESS : StringUtils.EMPTY );

        sbSQL.append( SQL_GROUP_BY_ENTRY_ENTRY_TYPE );
        sbSQL.append( SQL_ORDER_BY_POSITION );

        DAOUtil daoUtil = new DAOUtil( sbSQL.toString( ), plugin );
        int nIndex = 1;

        if ( filter.containsIdResource( ) )
        {
            daoUtil.setInt( nIndex++, filter.getIdResource( ) );
        }

        if ( filter.containsResourceType( ) )
        {
            daoUtil.setString( nIndex++, filter.getResourceType( ) );
        }

        if ( filter.containsIdEntryParent( ) )
        {
            daoUtil.setInt( nIndex++, filter.getIdEntryParent( ) );
        }

        if ( filter.containsIdIsGroup( ) )
        {
            if ( filter.getIdIsGroup( ) == 0 )
            {
                daoUtil.setBoolean( nIndex++, false );
            }
            else
            {
                daoUtil.setBoolean( nIndex++, true );
            }
        }

        if ( filter.containsIdField( ) )
        {
            daoUtil.setInt( nIndex++, filter.getIdFieldDepend( ) );
        }

        if ( filter.containsIdEntryType( ) )
        {
            daoUtil.setInt( nIndex++, filter.getIdEntryType( ) );
        }

        if ( filter.containsIdIsComment( ) )
        {
            if ( filter.getIdIsComment( ) == 0 )
            {
                daoUtil.setBoolean( nIndex++, false );
            }
            else
            {
                daoUtil.setBoolean( nIndex++, true );
            }
        }

        if ( filter.containsIsOnlyDisplayInBack( ) )
        {
            if ( filter.getIsOnlyDisplayInBack( ) == 0 )
            {
                daoUtil.setBoolean( nIndex++, false );
            }
            else
            {
                daoUtil.setBoolean( nIndex++, true );
            }
        }
        
        if ( filter.containsIsIndexed( ) )
        {
            if ( filter.getIsIndexed( ) == 0 )
            {
                daoUtil.setBoolean( nIndex++, false );
            }
            else
            {
                daoUtil.setBoolean( nIndex++, true );
            }
        }
        
        if ( filter.containsIsShownInCompleteness( ) )
        {
        	if ( filter.getIsShownInCompleteness( ) == 0 )
            {
                daoUtil.setBoolean( nIndex++, false );
            }
            else
            {
                daoUtil.setBoolean( nIndex++, true );
            }
        }

        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            entryList.add( getEntryValues( daoUtil ) );
        }

        daoUtil.free( );

        for ( Entry entryCreated : entryList )
        {
            entryCreated.setNumberConditionalQuestion( numberConditionalQuestion( entryCreated.getIdEntry( ), plugin ) );
        }

        return entryList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectNumberEntryByFilter( EntryFilter filter, Plugin plugin )
    {
        int nNumberEntry = 0;
        StringBuilder sbSQL = new StringBuilder( SQL_QUERY_SELECT_NUMBER_ENTRY_BY_FILTER );
        sbSQL.append( ( filter.containsIdResource( ) ) ? SQL_FILTER_ID_RESOURCE : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIdEntryParent( ) ) ? SQL_FILTER_ID_PARENT : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsEntryParentNull( ) ) ? SQL_FILTER_ID_PARENT_IS_NULL : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIdIsGroup( ) ) ? SQL_FILTER_IS_GROUP : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIdIsComment( ) ) ? SQL_FILTER_IS_COMMENT : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIdField( ) ) ? SQL_FILTER_ID_FIELD_DEPEND : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIdEntryType( ) ) ? SQL_FILTER_ID_TYPE : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIsOnlyDisplayInBack( ) ) ? SQL_FILTER_IS_ONLY_DISPLAY_IN_BACK : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIsEditableBack( ) ) ? SQL_FILTER_IS_EDITABLE_BACK : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIsIndexed( ) ) ? SQL_FILTER_IS_INDEXED : StringUtils.EMPTY );
        sbSQL.append( ( filter.containsIsShownInCompleteness( ) ) ? SQL_FILTER_IS_SHOWN_COMPLETNESS : StringUtils.EMPTY );
        
        sbSQL.append( SQL_GROUP_BY_POSITION );
        sbSQL.append( SQL_ORDER_BY_POSITION );

        DAOUtil daoUtil = new DAOUtil( sbSQL.toString( ), plugin );
        int nIndex = 1;

        if ( filter.containsIdResource( ) )
        {
            daoUtil.setInt( nIndex, filter.getIdResource( ) );
            nIndex++;
        }

        if ( filter.containsIdEntryParent( ) )
        {
            daoUtil.setInt( nIndex, filter.getIdEntryParent( ) );
            nIndex++;
        }

        if ( filter.containsIdIsGroup( ) )
        {
            if ( filter.getIdIsGroup( ) == 0 )
            {
                daoUtil.setBoolean( nIndex, false );
            }
            else
            {
                daoUtil.setBoolean( nIndex, true );
            }

            nIndex++;
        }

        if ( filter.containsIdIsComment( ) )
        {
            if ( filter.getIdIsComment( ) == 0 )
            {
                daoUtil.setBoolean( nIndex, false );
            }
            else
            {
                daoUtil.setBoolean( nIndex, true );
            }

            nIndex++;
        }

        if ( filter.containsIdField( ) )
        {
            daoUtil.setInt( nIndex, filter.getIdFieldDepend( ) );
            nIndex++;
        }

        if ( filter.containsIdEntryType( ) )
        {
            daoUtil.setInt( nIndex, filter.getIdEntryType( ) );
            nIndex++;
        }

        if ( filter.containsIsOnlyDisplayInBack( ) )
        {
            if ( filter.getIsOnlyDisplayInBack( ) == 0 )
            {
                daoUtil.setBoolean( nIndex++, false );
            }
            else
            {
                daoUtil.setBoolean( nIndex++, true );
            }
        }
        
        if ( filter.containsIsIndexed( ) )
        {
            if ( filter.getIsIndexed( ) == 0 )
            {
                daoUtil.setBoolean( nIndex++, false );
            }
            else
            {
                daoUtil.setBoolean( nIndex++, true );
            }
        }
        
        if ( filter.containsIsShownInCompleteness( ) )
        {
        	if ( filter.getIsShownInCompleteness( ) == 0 )
            {
                daoUtil.setBoolean( nIndex++, false );
            }
            else
            {
                daoUtil.setBoolean( nIndex++, true );
            }
        }

        daoUtil.executeQuery( );

        if ( daoUtil.next( ) )
        {
            nNumberEntry = daoUtil.getInt( 1 );
        }

        daoUtil.free( );

        return nNumberEntry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> findEntriesWithoutParent( Plugin plugin, int nIdResource, String strResourceType )
    {
        List<Entry> listResult = new ArrayList<Entry>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_ENTRIES_PARENT_NULL, plugin );
        daoUtil.setInt( 1, nIdResource );
        daoUtil.setString( 2, strResourceType );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            listResult.add( getEntryValues( daoUtil ) );
        }

        daoUtil.free( );

        for ( Entry entryCreated : listResult )
        {
            entryCreated.setNumberConditionalQuestion( numberConditionalQuestion( entryCreated.getIdEntry( ), plugin ) );
        }

        return listResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry findByOrderAndIdFieldAndIdResource( Plugin plugin, int nOrder, int nIdField, int nIdResource, String strResourceType )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_ENTRY_CONDITIONAL_WITH_ORDER_BY_FIELD, plugin );
        daoUtil.setInt( 1, nOrder );
        daoUtil.setInt( 2, nIdField );
        daoUtil.setInt( 3, nIdResource );
        daoUtil.executeQuery( );

        Entry entry = null;

        if ( daoUtil.next( ) )
        {
            entry = getEntryValues( daoUtil );
        }

        daoUtil.free( );

        if ( entry != null )
        {
            entry.setNumberConditionalQuestion( numberConditionalQuestion( entry.getIdEntry( ), plugin ) );
        }

        return entry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementOrderByOne( Plugin plugin, int nOrder, int nIdField, int nIdResource, String strResourceType )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DECREMENT_ORDER_CONDITIONAL, plugin );
        daoUtil.setInt( 1, nOrder );
        daoUtil.setInt( 2, nIdField );
        daoUtil.setInt( 3, nIdResource );
        daoUtil.setString( 4, strResourceType );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * Generates a new primary key
     *
     * @param plugin
     *            the plugin
     * @return The new primary key
     */
    private int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin );
        daoUtil.executeQuery( );

        int nKey;

        if ( !daoUtil.next( ) )
        {
            // if the table is empty
            nKey = 1;
        }

        nKey = daoUtil.getInt( 1 ) + 1;
        daoUtil.free( );

        return nKey;
    }

    /**
     * Generates a new entry position
     * 
     * @param plugin
     *            the plugin
     * @param entry
     *            the entry
     * @return the new entry position
     */
    private int newPosition( Entry entry, Plugin plugin )
    {
        DAOUtil daoUtil = null;
        int nPos;

        if ( entry.getFieldDepend( ) == null )
        {
            daoUtil = new DAOUtil( SQL_QUERY_NEW_POSITION, plugin );

            daoUtil.setInt( 1, entry.getIdResource( ) );
            daoUtil.setString( 2, entry.getResourceType( ) );
            daoUtil.executeQuery( );

            if ( !daoUtil.next( ) )
            {
                // if the table is empty
                nPos = 1;
            }

            nPos = daoUtil.getInt( 1 ) + 1;
            daoUtil.free( );
        }
        else
        {
            // case of conditional question only
            nPos = 0;
        }

        return nPos;
    }

    /**
     * Generates a new entry position
     * 
     * @param plugin
     *            the plugin
     * @param entry
     *            the entry
     * @return the new entry position
     */
    private int newPositionConditional( Entry entry, Plugin plugin )
    {
        DAOUtil daoUtil = null;
        int nPos;

        if ( entry.getFieldDepend( ) != null )
        {
            // case of conditional question only
            daoUtil = new DAOUtil( SQL_QUERY_NEW_POSITION_CONDITIONAL_QUESTION, plugin );

            daoUtil.setInt( 1, entry.getFieldDepend( ).getIdField( ) );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                // if the table is empty
                nPos = daoUtil.getInt( 1 ) + 1;
            }
            else
            {
                nPos = 1;
            }

            daoUtil.free( );
        }
        else
        {
            nPos = 0;
        }

        return nPos;
    }

    /**
     * Return the number of conditional question who are associate to the entry
     * 
     * @param nIdEntry
     *            the id of the entry
     * @param plugin
     *            the plugin
     * @return the number of conditional question
     */
    private int numberConditionalQuestion( int nIdEntry, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NUMBER_CONDITIONAL_QUESTION, plugin );
        daoUtil.setInt( 1, nIdEntry );
        daoUtil.executeQuery( );

        int nNumberConditionalQuestion = 0;

        if ( daoUtil.next( ) )
        {
            nNumberConditionalQuestion = daoUtil.getInt( 1 );
        }

        daoUtil.free( );

        return nNumberConditionalQuestion;
    }

    /**
     * Get values of an entry from the current row of a daoUtil. The class to daoUtil.next( ) will NOT be made by this method.
     * 
     * @param daoUtil
     *            The DAOUtil
     * @return The entry, or null if the entry was not found
     */
    private Entry getEntryValues( DAOUtil daoUtil )
    {
        Entry entry;

        int nIndex = 1;
        EntryType entryType = new EntryType( );
        entryType.setIdType( daoUtil.getInt( nIndex++ ) );
        entryType.setTitle( daoUtil.getString( nIndex++ ) );
        entryType.setGroup( daoUtil.getBoolean( nIndex++ ) );
        entryType.setComment( daoUtil.getBoolean( nIndex++ ) );
        entryType.setBeanName( daoUtil.getString( nIndex++ ) );
        entryType.setMyLuteceUser( daoUtil.getBoolean( nIndex++ ) );
        entryType.setIconName( daoUtil.getString( nIndex++ ) );

        entry = new Entry( );

        entry.setEntryType( entryType );
        entry.setIdEntry( daoUtil.getInt( nIndex++ ) );

        entry.setIdResource( daoUtil.getInt( nIndex++ ) );
        entry.setResourceType( daoUtil.getString( nIndex++ ) );

        if ( daoUtil.getObject( nIndex++ ) != null )
        {
            Entry entryParent = new Entry( );
            entryParent.setIdEntry( daoUtil.getInt( nIndex - 1 ) );
            entry.setParent( entryParent );
        }

        entry.setCode( daoUtil.getString( nIndex++ ) );
        entry.setTitle( daoUtil.getString( nIndex++ ) );
        entry.setHelpMessage( daoUtil.getString( nIndex++ ) );
        entry.setComment( daoUtil.getString( nIndex++ ) );
        entry.setMandatory( daoUtil.getBoolean( nIndex++ ) );
        entry.setFieldInLine( daoUtil.getBoolean( nIndex++ ) );
        entry.setPosition( daoUtil.getInt( nIndex++ ) );

        if ( daoUtil.getObject( nIndex++ ) != null )
        {
            Field fieldDepend = new Field( );
            fieldDepend.setIdField( daoUtil.getInt( nIndex - 1 ) );
            entry.setFieldDepend( fieldDepend );
        }

        entry.setConfirmField( daoUtil.getBoolean( nIndex++ ) );
        entry.setConfirmFieldTitle( daoUtil.getString( nIndex++ ) );
        entry.setUnique( daoUtil.getBoolean( nIndex++ ) );
        entry.setMapProvider( MapProviderManager.getMapProvider( daoUtil.getString( nIndex++ ) ) );
        entry.setCSSClass( daoUtil.getString( nIndex++ ) );

        if ( daoUtil.getInt( nIndex++ ) > 0 )
        {
            entry.setPosition( daoUtil.getInt( nIndex - 1 ) );
        }

        entry.setErrorMessage( daoUtil.getString( nIndex++ ) );
        entry.setNumberRow( daoUtil.getInt( nIndex++ ) );
        entry.setNumberColumn( daoUtil.getInt( nIndex++ ) );
        entry.setRoleAssociated( daoUtil.getBoolean( nIndex++ ) );
        entry.setOnlyDisplayInBack( daoUtil.getBoolean( nIndex++ ) );
        entry.setEditableBack( daoUtil.getBoolean( nIndex++ ) );
        entry.setIndexed( daoUtil.getBoolean( nIndex++ ) );
        entry.setShownInCompleteness( daoUtil.getBoolean( nIndex++ ) );

        return entry;
    }

    /**
     * Return the trim of the title of the entry or null if the entry doesn't have a title
     * 
     * @param entry
     *            The entry to retrieve the title from
     * @return the trim of the title of the entry or null if the entry doesn't have a title
     */
    private String trimEntryTitle( Entry entry )
    {
        String strEntryTitle = entry.getTitle( );

        if ( strEntryTitle != null )
        {
            strEntryTitle = strEntryTitle.trim( );
        }

        return strEntryTitle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, String> findEntryByForm( Plugin plugin, int nIdForm )
    {
        Map<Integer, String> listResult = new HashMap<Integer, String>( );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ENTRY_BY_FORM, plugin );
        daoUtil.setInt( 1, nIdForm );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            listResult.put( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
        }

        daoUtil.free( );

        return listResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryValueByIdResponse( Plugin plugin, int nIdEntry, int nIdResponse )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ENTRY_VALUE, plugin );
        daoUtil.setInt( 1, nIdEntry );
        daoUtil.setInt( 2, nIdResponse );
        daoUtil.executeQuery( );

        String val = null;

        if ( daoUtil.next( ) )
        {
            val = daoUtil.getString( 1 );
        }

        daoUtil.free( );

        return val;
    }
}
