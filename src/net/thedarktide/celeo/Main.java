package net.thedarktide.celeo;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main
{

	private static File databaseFile = null;
	private static Connection connection = null;
	private static List<String> ignore = new ArrayList<String>(Arrays.asList("Celeo"));

	@SuppressWarnings("boxing")
	public static void main(String[] args) throws SQLException, ClassNotFoundException
	{
		List<String> names = new ArrayList<String>();
		System.out.println("Starting... \n\n");
		databaseFile = new File("C:/Users/Matt/Desktop/Logs.db");
		if (!databaseFile.exists())
		{
			System.err.println("Cannot find database.");
			System.exit(-1);
		}
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
		Statement stat = connection.createStatement();
		ResultSet rs = stat.executeQuery("Select name,time,type from `logs` where type like 'openChest %' and time > '1366221489'");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
		String name = "";
		String type = "";
		while (rs.next())
		{
			name = rs.getString("name");
			if (ignore.contains(name))
				continue;
			type = rs.getString("type");
			int[] data = new int[3];
			for (int i = 0; i != 3; i++)
			{
//				System.out.println(i + " " + type.split(" ")[i + 1]);
				data[i] = Integer.valueOf(type.split(" ")[i + 1]).intValue();
			}
//			System.out.printf("%d %d %d\n", data[0], data[1], data[2]);
			if (data[0] != -36)
				continue;
			if (data[1] != 74)
				continue;
			if (data[2] < -10 || data[2] > 10)
				continue;
			// openChest x y z
			if (!names.contains(name))
				names.add(name);
			System.out.printf("Name: %s, Time: %s, Type: %s\n\n", name, sdf.format(rs.getLong("time") * 1000), type);
		}
		if (!rs.isClosed())
			rs.close();
		for (String n : names)
		{
			System.out.println(n);
		}
		System.out.println("Done");
	}

}