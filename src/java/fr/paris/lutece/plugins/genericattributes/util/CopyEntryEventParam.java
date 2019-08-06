package fr.paris.lutece.plugins.genericattributes.util;

import fr.paris.lutece.portal.business.event.IEventParam;

/**
 * Parameter for the Copy Event Param.
 */
public class CopyEntryEventParam implements IEventParam<Integer>
{
	private final Integer _oldEntryId;
	
	public CopyEntryEventParam( Integer oldEntryId )
	{
		_oldEntryId = oldEntryId;
	}

	@Override
	public Integer getValue( )
	{
		return _oldEntryId;
	}
}
