import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.h2.Driver;

import com.sun.jna.platform.win32.Advapi32Util.EventLogIterator;
import com.sun.jna.platform.win32.Advapi32Util.EventLogRecord;


public class LocalTest {
	public static void main(String[] args) throws Exception {
		
		EventLogIterator it = new EventLogIterator("", "Security", 0x8);
		int i = 0;
		while(it.hasNext() && i++ < 20) {
			EventLogRecord rec = it.next();
			Date d = new Date(rec.getRecord().TimeGenerated.longValue() * 1000);
			System.out.format("%d %s%n", rec.getEventId(), d);
		}
	}
}
