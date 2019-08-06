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

import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Data Access methods for Response objects
 */
public final class ResponseDAO implements IResponseDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT_RESPONSE = "SELECT resp.id_response, resp.response_value, type.class_name, ent.id_type, ent.id_entry, ent.title, ent.code, "
            + " resp.iteration_number, resp.id_field, resp.id_file, resp.status FROM genatt_response resp";
    private static final String SQL_QUERY_FIND_BY_PRIMARY_KEY = SQL_QUERY_SELECT_RESPONSE + ", genatt_entry ent, genatt_entry_type type "
            + " WHERE resp.id_response = ? and resp.id_entry = ent.id_entry and ent.id_type = type.id_type ";
    private static final String SQL_QUERY_SELECT_RESPONSE_BY_FILTER = SQL_QUERY_SELECT_RESPONSE + ", genatt_entry ent, genatt_entry_type type "
            + " WHERE resp.id_entry = ent.id_entry and ent.id_type = type.id_type ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO genatt_response ( "
            + " response_value, id_entry, iteration_number, id_field, id_file, status ) VALUES ( ?,?,?,?,?,?)";
    private static final String SQL_QUERY_UPDATE = "UPDATE genatt_response SET response_value = ?, id_entry = ?, iteration_number = ?, id_field = ?, id_file = ?, status = ? WHERE id_response = ?";
    private static final String SQL_QUERY_DELETE = "DELETE FROM genatt_response WHERE id_response = ? ";
    private static final String SQL_QUERY_SELECT_COUNT_RESPONSE_BY_ID_ENTRY = " SELECT field.title, COUNT( resp.id_response )"
            + " FROM genatt_entry e LEFT JOIN genatt_field field ON ( e.id_entry = field.id_entry ) LEFT JOIN genatt_response resp on ( resp.id_field = field.id_field ) "
            + " WHERE e.id_entry = ? GROUP BY field.id_field ORDER BY field.pos ";

    // Special query in order to sort numerically and not alphabetically (thus avoiding list like 1, 10, 11, 2, ... instead of 1, 2, ..., 10, 11)
    private static final String SQL_QUERY_SELECT_MAX_NUMBER = " SELECT fr.response_value FROM genatt_response fr "
            + " WHERE fr.id_entry = ? ORDER BY CAST(fr.response_value AS DECIMAL) DESC LIMIT 1 ";
    private static final String SQL_FILTER_ID_RESOURCE = " AND ent.id_resource = ? ";
    private static final String SQL_FILTER_ID_ENTRY = " AND resp.id_entry = ? ";
    private static final String SQL_FILTER_ID_FIELD = " AND resp.id_field = ? ";
    private static final String SQL_FILTER_CODE_ENTRY = " AND ent.code = ? ";
    private static final String SQL_FILTER_RESPONSE_VALUE = " AND resp.response_value = ? ";
    private static final String SQL_FILTER_ID_RESPONSE = " resp.id_response ";
    private static final String SQL_FILTER_MULTI_ID_RESPONSE = "AND resp.id_response IN ";
    private static final String SQL_ORDER_BY = " ORDER BY ";
    private static final String SQL_ASC = " ASC ";
    private static final String SQL_DESC = " DESC ";

 

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void insert( Response response, Plugin plugin )
    {
        int nIndex = 1;

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin );
       // daoUtil.setInt( nIndex++, response.getIdResponse( ) );
        daoUtil.setString( nIndex++, removeInvalidChars( response.getResponseValue( ) ) );
        daoUtil.setInt( nIndex++, response.getEntry( ).getIdEntry( ) );
        daoUtil.setInt( nIndex++, response.getIterationNumber( ) );

        if ( response.getField( ) != null )
        {
            daoUtil.setInt( nIndex++, response.getField( ).getIdField( ) );
        }
        else
        {
            daoUtil.setIntNull( nIndex++ );
        }

        if ( response.getFile( ) != null )
        {
            daoUtil.setInt( nIndex++, response.getFile( ).getIdFile( ) );
        }
        else
        {
            daoUtil.setIntNull( nIndex++ );
        }

        daoUtil.setInt( nIndex, Response.CONSTANT_STATUS_ACTIVE );

        daoUtil.executeUpdate( );
        
        if ( daoUtil.nextGeneratedKey( ) )
        {
        	response.setIdResponse( daoUtil.getGeneratedKeyInt( 1 ) );
        }

        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response load( int nIdResponse, Plugin plugin )
    {
        Response response = null;

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_BY_PRIMARY_KEY, plugin );
        daoUtil.setInt( 1, nIdResponse );
        daoUtil.executeQuery( );

        if ( daoUtil.next( ) )
        {
            response = getResponseFromDAOUtil( daoUtil );
        }

        daoUtil.free( );

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nIdResponse, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nIdResponse );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( Response response, Plugin plugin )
    {
        int nIndex = 1;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );

        daoUtil.setString( nIndex++, response.getResponseValue( ) );
        daoUtil.setInt( nIndex++, response.getEntry( ).getIdEntry( ) );
        daoUtil.setInt( nIndex++, response.getIterationNumber( ) );

        if ( response.getField( ) != null )
        {
            daoUtil.setInt( nIndex++, response.getField( ).getIdField( ) );
        }
        else
        {
            daoUtil.setIntNull( nIndex++ );
        }

        if ( response.getFile( ) != null )
        {
            daoUtil.setInt( nIndex++, response.getFile( ).getIdFile( ) );
        }
        else
        {
            daoUtil.setIntNull( nIndex++ );
        }

        daoUtil.setInt( nIndex++, response.getStatus( ) );

        daoUtil.setInt( nIndex, response.getIdResponse( ) );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Response> selectListByFilter( ResponseFilter filter, Plugin plugin )
    {
        List<Response> responseList = new ArrayList<Response>( );

        StringBuilder sbSQL = new StringBuilder( SQL_QUERY_SELECT_RESPONSE_BY_FILTER );

        if ( filter.containsIdResource( ) )
        {
            sbSQL.append( SQL_FILTER_ID_RESOURCE );
        }

        if ( filter.containsIdEntry( ) )
        {
            sbSQL.append( SQL_FILTER_ID_ENTRY );
        }

        if ( filter.containsIdField( ) )
        {
            sbSQL.append( SQL_FILTER_ID_FIELD );
        }
        
        if ( filter.containsCodeEntry( ) )
        {
        	sbSQL.append( SQL_FILTER_CODE_ENTRY );
        }
        
        if ( filter.containsResponseValue( ) )
        {
        	sbSQL.append( SQL_FILTER_RESPONSE_VALUE );
        }

        if ( filter.containsListIdResource( ) )
        {
            StringBuilder sb = new StringBuilder( SQL_FILTER_MULTI_ID_RESPONSE + " (" );
            int count = 1;

            for ( Integer i : filter.getListId( ) )
            {
                if ( count != filter.getListId( ).size( ) )
                {
                    sb = sb.append( i + "," );
                }
                else
                {
                    sb = sb.append( i );
                }

                count++;
            }

            sb = sb.append( ")" );
            sbSQL.append( sb.toString( ) );
        }

        sbSQL.append( SQL_ORDER_BY );
        sbSQL.append( ( filter.containsOrderBy( ) ) ? filter.getOrderBy( ) : SQL_FILTER_ID_RESPONSE );
        sbSQL.append( ( filter.isOrderByAsc( ) ) ? SQL_ASC : SQL_DESC );

        DAOUtil daoUtil = new DAOUtil( sbSQL.toString( ), plugin );
        int nIndex = 1;

        if ( filter.containsIdResource( ) )
        {
            daoUtil.setInt( nIndex++, filter.getIdResource( ) );
        }

        if ( filter.containsIdEntry( ) )
        {
            daoUtil.setInt( nIndex++, filter.getIdEntry( ) );
        }

        if ( filter.containsIdField( ) )
        {
            daoUtil.setInt( nIndex++, filter.getIdField( ) );
        }
        
        if ( filter.containsCodeEntry( ) )
        {
        	daoUtil.setString( nIndex++, filter.getCodeEntry( ) );
        }
        
        if ( filter.containsResponseValue( ) )
        {
        	daoUtil.setString( nIndex++, filter.getResponseValue( ) );
        }

        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            responseList.add( getResponseFromDAOUtil( daoUtil ) );
        }

        daoUtil.free( );

        return responseList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<StatisticEntrySubmit> getStatisticByIdEntry( int nIdEntry, Plugin plugin )
    {
        List<StatisticEntrySubmit> listStatisticEntrySubmit = new ArrayList<StatisticEntrySubmit>( );
        StatisticEntrySubmit statisticEntrySubmit;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_COUNT_RESPONSE_BY_ID_ENTRY, plugin );
        daoUtil.setInt( 1, nIdEntry );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            statisticEntrySubmit = new StatisticEntrySubmit( );
            statisticEntrySubmit.setFieldLibelle( daoUtil.getString( 1 ) );
            statisticEntrySubmit.setNumberResponse( daoUtil.getInt( 2 ) );
            listStatisticEntrySubmit.add( statisticEntrySubmit );
        }

        daoUtil.free( );

        return listStatisticEntrySubmit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxNumber( int nIdEntry, Plugin plugin )
    {
        int nIndex = 1;
        int nKey = 1;
        try (DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_MAX_NUMBER, plugin ))
        {
	        daoUtil.setInt( nIndex++, nIdEntry );
	        daoUtil.executeQuery( );
	
	        if ( daoUtil.next( ) )
	        {
	            nKey = daoUtil.getInt( 1 ) + 1;
	        }

        }
        return nKey;
    }

    /**
     * Get a response from a DAOUtil.
     * 
     * @param daoUtil
     *            The daoUtil to get data from. Note that the DAOUtil will NOT be free by this method
     * @return The response
     */
    private Response getResponseFromDAOUtil( DAOUtil daoUtil )
    {
        int nIndex = 1;

        Response response = new Response( );
        response.setIdResponse( daoUtil.getInt( nIndex++ ) );

        response.setResponseValue( daoUtil.getString( nIndex++ ) );

        EntryType entryType = new EntryType( );
        entryType.setBeanName( daoUtil.getString( nIndex++ ) );
        entryType.setIdType( daoUtil.getInt( nIndex++ ) );

        Entry entry = new Entry( );

        entry.setEntryType( entryType );
        entry.setIdEntry( daoUtil.getInt( nIndex++ ) );
        entry.setTitle( daoUtil.getString( nIndex++ ) );
        entry.setCode( daoUtil.getString( nIndex++ ) );
        response.setEntry( entry );

        response.setIterationNumber( daoUtil.getInt( nIndex++ ) );

        // Get field if it exists
        if ( daoUtil.getObject( nIndex ) != null )
        {
            Field field = new Field( );
            field.setIdField( daoUtil.getInt( nIndex ) );
            response.setField( field );
        }

        nIndex++;

        // Get file if it exists
        if ( daoUtil.getObject( nIndex ) != null )
        {
            File file = new File( );
            file.setIdFile( daoUtil.getInt( nIndex ) );
            response.setFile( file );
        }

        nIndex++;
        response.setStatus( daoUtil.getInt( nIndex ) );

        return response;
    }

    /**
     * Keep only valid caracters
     * 
     * @param s
     *            the string to control
     * @return the string with only valid caracters
     */
    private String removeInvalidChars( String s )
    {
        if ( s == null )
        {
            return null;
        }

        StringBuilder sb = new StringBuilder( );

        for ( int i = 0; i < s.length( ); i++ )
        {
            Character c = s.charAt( i );

            if ( Character.getType( c ) == Character.CONTROL )
            {
                // Check all invalid char
                if ( !( ( c == ' ' ) || ( c == '\n' ) || ( c == '\r' ) || ( c == '\t' ) ) )
                {
                    // Remove all non spaces char
                    continue;
                }
            }

            sb.append( c );
        }

        return sb.toString( );
    }
}
