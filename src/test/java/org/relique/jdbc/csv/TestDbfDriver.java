/*
CsvJdbc - a JDBC driver for CSV files
Copyright (C) 2001  Jonathan Ackerman

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.relique.jdbc.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class is used to test the CsvJdbc driver.
 *
 * @author Mario Frasca
 */
public class TestDbfDriver
{
	private static String filePath;
	private static DateFormat toUTC;

	@BeforeClass
	public static void setUp() throws IOException
	{
		filePath = (new File(System.getProperty("sample.files.location")).getCanonicalPath());
		//filePath = ".." + File.separator + "src" + File.separator + "testdata";
		if (!new File(filePath).isDirectory())
			filePath = "src" + File.separator + "testdata";
		assertTrue("Sample files directory not found: " + filePath, new File(filePath).isDirectory());

		// load CSV driver
		try
		{
			Class.forName("org.relique.jdbc.csv.CsvDriver");
		}
		catch (ClassNotFoundException e)
		{
			fail("Driver is not in the CLASSPATH -> " + e);
		}
		toUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		toUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * This creates several sentences with where and tests they work
	 *
	 * @throws SQLException
	 */
	@Test
	public void testGetAll() throws SQLException
	{
		Properties props = new Properties();
		props.put("fileExtension", ".dbf");

		Connection conn = DriverManager.getConnection("jdbc:relique:csv:"
				+ filePath, props);

		Statement stmt = conn.createStatement();

		ResultSet results = stmt
				.executeQuery("SELECT * FROM sample");
		assertTrue(results.next());
		assertEquals("The name is wrong", "Gianni", results
				.getString("Name"));
		assertTrue(results.next());
		assertEquals("The name is wrong", "Reinout", results
				.getString("Name"));
		assertTrue(results.next());
		assertEquals("The name is wrong", "Alex", results
				.getString("Name"));
		assertTrue(results.next());
		assertEquals("The name is wrong", "Gianni", results
				.getString("Name"));
		assertTrue(results.next());
		assertEquals("The name is wrong", "Mario", results
				.getString("Name"));
		assertFalse(results.next());
	}

	@Test
	public void testWhereOp() throws SQLException
	{
		Properties props = new Properties();
		props.put("fileExtension", ".dbf");

		Connection conn = DriverManager.getConnection("jdbc:relique:csv:"
				+ filePath, props);

		Statement stmt = conn.createStatement();

		ResultSet results = stmt
				.executeQuery("SELECT * FROM sample WHERE key = 'op'");
		assertTrue(results.next());
		assertEquals("The name is wrong", "Gianni", results
				.getString("Name"));
		assertEquals("The name is wrong", "debian", results
				.getString("value"));
		assertTrue(results.next());
		assertEquals("The name is wrong", "Reinout", results
				.getString("Name"));
		assertEquals("The name is wrong", "ubuntu", results
				.getString("value"));
		assertTrue(results.next());
		assertEquals("The name is wrong", "Alex", results
				.getString("Name"));
		assertEquals("The name is wrong", "windows", results
				.getString("value"));
		assertFalse(results.next());
	}

	@Test
	public void testWhereTodo() throws SQLException
	{
		Properties props = new Properties();
		props.put("fileExtension", ".dbf");

		Connection conn = DriverManager.getConnection("jdbc:relique:csv:"
				+ filePath, props);

		Statement stmt = conn.createStatement();

		ResultSet results = stmt
				.executeQuery("SELECT * FROM sample WHERE key = 'todo'");
		assertTrue(results.next());
		assertEquals("The name is wrong", "Gianni", results
				.getString("Name"));
		assertEquals("The name is wrong", "none", results
				.getString("value"));
		assertTrue(results.next());
		assertEquals("The name is wrong", "Mario", results
				.getString("Name"));
		assertEquals("The name is wrong", "sleep", results
				.getString("value"));
		assertFalse(results.next());
	}

	@Test
	public void testWhereWithIsNull() throws SQLException
	{
		Properties props = new Properties();
		props.put("fileExtension", ".dbf");

		Connection conn = DriverManager.getConnection("jdbc:relique:csv:"
				+ filePath, props);

		Statement stmt = conn.createStatement();

		ResultSet results = stmt
				.executeQuery("SELECT * FROM fox_samp WHERE COWNNAME IS NULL");
		assertFalse(results.next());
	}

	@Test
	public void testTypedColumns() throws SQLException
	{
		Properties props = new Properties();
		props.put("fileExtension", ".dbf");

		Connection conn = DriverManager.getConnection("jdbc:relique:csv:"
				+ filePath, props);

		Statement stmt = conn.createStatement();

		ResultSet results = stmt
				.executeQuery("SELECT * FROM fox_samp");
		assertTrue(results.next());
		ResultSetMetaData metadata = results.getMetaData();

		assertEquals("Incorrect Table Name", "fox_samp", metadata
				.getTableName(0));
		assertEquals("Incorrect Column Count", 57, metadata.getColumnCount());
		assertEquals("Incorrect Column Type", Types.VARCHAR, metadata.getColumnType(1));
		assertEquals("Incorrect Column Type", Types.BOOLEAN, metadata.getColumnType(2));
		assertEquals("Incorrect Column Type", Types.DOUBLE, metadata.getColumnType(3));
	}

	@Test
	public void testColumnDisplaySizes() throws SQLException
	{
		Properties props = new Properties();
		props.put("fileExtension", ".dbf");

		Connection conn = DriverManager.getConnection("jdbc:relique:csv:"
				+ filePath, props);

		Statement stmt = conn.createStatement();

		ResultSet results = stmt
				.executeQuery("SELECT * FROM fox_samp");
		assertTrue(results.next());
		ResultSetMetaData metadata = results.getMetaData();

		assertEquals("Incorrect Column Size", 11, metadata.getColumnDisplaySize(1));
		assertEquals("Incorrect Column Size", 1, metadata.getColumnDisplaySize(2));
		assertEquals("Incorrect Column Size", 4, metadata.getColumnDisplaySize(3));
	}

	@Test
	public void testDatabaseMetadataTables() throws SQLException
	{
		Properties props = new Properties();
		props.put("fileExtension", ".dbf");

		Connection conn = DriverManager.getConnection("jdbc:relique:csv:"
				+ filePath, props);
		DatabaseMetaData metadata = conn.getMetaData();
		ResultSet results = metadata.getTables(null, null, "*", null);
		Set<String> target = new HashSet<String>();
		target.add("sample");
		target.add("fox_samp");
		target.add("hotel");

		Set<String> current = new HashSet<String>();
		assertTrue(results.next());
		current.add(results.getString("TABLE_NAME"));
		assertTrue(results.next());
		current.add(results.getString("TABLE_NAME"));
		assertTrue(results.next());
		current.add(results.getString("TABLE_NAME"));
		assertFalse(results.next());

		assertEquals("Incorrect table names", target, current);
	}

	@Test
	public void testDatabaseMetadataColumns() throws SQLException
	{
		Properties props = new Properties();
		props.put("fileExtension", ".dbf");

		Connection conn = DriverManager.getConnection("jdbc:relique:csv:"
				+ filePath, props);
		DatabaseMetaData metadata = conn.getMetaData();
		ResultSet results = metadata.getColumns(null, null, "sample", "*");
		assertTrue(results.next());
		assertEquals("Incorrect table name", "sample", results.getString("TABLE_NAME"));
		assertEquals("Incorrect column name", "NAME", results.getString("COLUMN_NAME"));
		assertEquals("Incorrect column type", Types.VARCHAR, results.getInt("DATA_TYPE"));
		assertEquals("Incorrect column type", "String", results.getString("TYPE_NAME"));
		assertEquals("Incorrect ordinal position", 1, results.getInt("ORDINAL_POSITION"));
		assertTrue(results.next());
		assertEquals("Incorrect column name", "KEY", results.getString(4));
	}

	@Test
	public void testGetNumeric() throws SQLException
	{
		Properties props = new Properties();
		props.put("fileExtension", ".dbf");

		Connection conn = DriverManager.getConnection("jdbc:relique:csv:" + filePath, props);

		Statement stmt = conn.createStatement();

		ResultSet results = stmt.executeQuery("SELECT * FROM fox_samp");
		assertTrue(results.next());
		assertEquals("The NCOUNTYCOD is wrong", 33, results.getByte("NCOUNTYCOD"));
		assertEquals("The NCOUNTYCOD is wrong", 33, results.getShort("NCOUNTYCOD"));
		assertEquals("The NTAXYEAR is wrong", 2011, results.getInt("NTAXYEAR"));
		assertEquals("The NNOTFCV is wrong", 0, results.getLong("NNOTFCV"));
		assertEquals("The NASSASSRAT is wrong", 7250, Math.round(results.getFloat("NASSASSRAT") * 1000));
		assertEquals("The NASSASSRAT is wrong", 7250, Math.round(results.getDouble("NASSASSRAT") * 1000));
		assertFalse(results.next());
	}

	@Test
	public void testGetDate() throws SQLException
	{
		Properties props = new Properties();
		props.put("fileExtension", ".dbf");

		Connection conn = DriverManager.getConnection("jdbc:relique:csv:" + filePath, props);

		Statement stmt = conn.createStatement();

		ResultSet results = stmt.executeQuery("SELECT DASSDATE FROM fox_samp");
		assertTrue(results.next());
		assertEquals("The DASSDATE is wrong", Date.valueOf("2012-12-25"), results.getDate(1));
	}

	@Test
	public void testGetTimestamp() throws SQLException
	{
		Properties props = new Properties();
		props.put("fileExtension", ".dbf");

		Connection conn = DriverManager.getConnection("jdbc:relique:csv:" + filePath, props);

		Statement stmt = conn.createStatement();

		ResultSet results = stmt.executeQuery("SELECT DASSDATE FROM fox_samp");
		assertTrue(results.next());
		assertEquals("The DASSDATE is wrong", Timestamp.valueOf("2012-12-25 00:00:00"),
			results.getTimestamp(1));
	}

	@Test
	public void testCharset() throws SQLException
	{
		Properties props = new Properties();
		props.put("fileExtension", ".dbf");
		props.put("charset", "ISO-8859-1");

		Connection conn = DriverManager.getConnection("jdbc:relique:csv:" + filePath, props);

		Statement stmt = conn.createStatement();

		ResultSet results = stmt.executeQuery("SELECT HOTELNAME FROM hotel");
		assertTrue(results.next());
		assertEquals("The HOTELNAME is wrong", "M\u00DCNCHEN HOTEL", results.getString(1));
		assertTrue(results.next());
		assertEquals("The HOTELNAME is wrong", "MALM\u00D6 INN", results.getString(1));
		assertTrue(results.next());
		assertEquals("The HOTELNAME is wrong", "K\u00D8BENHAVN HOTEL", results.getString(1));
		assertTrue(results.next());
		assertEquals("The HOTELNAME is wrong", "C\u00F3rdoba Hotel", results.getString(1));
		assertFalse(results.next());
	}
}