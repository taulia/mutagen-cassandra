package com.toddfast.mutagen.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.util.List;

import com.toddfast.mutagen.MutagenException;
import com.toddfast.mutagen.State;
import com.toddfast.mutagen.Subject;
import com.toddfast.mutagen.basic.SimpleState;

/**
 *
 * @author Todd Fast
 */
public class CassandraSubject implements Subject<Integer> {

	/**
	 *
	 *
	 */
	public CassandraSubject(Session session) {
		super();
		if (session==null) {
			throw new IllegalArgumentException(
				"Parameter \"keyspace\" cannot be null");
		}

		this.session = session;
	}


	/**
	 *
	 *
	 */
	public Session getSession() {
		return session;
	}
	/**
	 * create version table
	 *
	 */
	public void createSchemaVersionTable(){
		//createstatement
		String createStatement = "CREATE TABLE \""+  
		versionSchemaTable+
		"\"( versionid varchar, filename varchar,checksum varchar,"
		+ "execution_data timestamp,execution_time int,"
		+ "success boolean, PRIMARY KEY(versionid))";
		
		session.execute(createStatement);
	}

	/**
	 *  get current version record
	 */
	
	public ResultSet getVersionRecord(){
		//get version record
		String selectStatement = "SELECT * FROM \"" + 
									versionSchemaTable + "\"" + 
									limit + ";";
		return session.execute(selectStatement);
	}
	
	/**
	 * 
	 * 
	 */
	@Override
	public State<Integer> getCurrentState() {
		
		ResultSet results = null;
		try{
			results = getVersionRecord();
		}catch(Exception e){
			try{
				createSchemaVersionTable();
			}catch(Exception e2){
				throw new MutagenException("Could not create version table", e2);
			}
		}
		try {
			results = getVersionRecord();
		} catch (Exception e) {
			throw new MutagenException(
					"could not retreive Version table information", e);

		}

		List<Row> rows = results.all();

		int version = 0;
		for (Row r1 : rows) {
			int timestamp = r1.getInt("id");
			if (version < timestamp)
				version = timestamp;
		}

		return new SimpleState<Integer>(version);
	}




	////////////////////////////////////////////////////////////////////////////
	// Fields
	////////////////////////////////////////////////////////////////////////////

//	public static final ColumnFamily<String,String> VERSION_CF=
//		ColumnFamily.newColumnFamily(
//			"schema_version",
//			StringSerializer.get(),
//			StringSerializer.get(),
//			ByteBufferSerializer.get());
//	public static final String ROW_KEY="state";
//	public static final String VERSION_COLUMN="version";

	private Session session;   //session
	
	private String versionSchemaTable = "Version";
	private String limit = " limit " + 1_000_000_000;
}
