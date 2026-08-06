package com.github.marschall.fixedlenghtfile.batch;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.batch.infrastructure.item.database.ItemPreparedStatementSetter;

import com.github.marschall.fixedlenghtfile.ReadingLine;

public class FixedLengthPreparedStatementSetter implements ItemPreparedStatementSetter<ReadingLine> {
  
  // to be used with JdbcBatchItemWriter
  // use index based queries to avoid allocating SqlParameterSource
  // stream the content from the page cache to the socket buffer of the driver
  // avoid allocating the content on the heap

  @Override
  public void setValues(ReadingLine line, PreparedStatement ps) throws SQLException {
    int i = 1;
    // CLOB -> jCharacterStream
    ps.setCharacterStream(i++, line.asReader(), line.getLength());
    
  }

}
