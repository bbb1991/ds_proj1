insert into chunk (datatype, locked, original_name, FILE_SIZE, parent_id, seq_no)
  SELECT T.* from (SELECT 1 datatype, false locked, '/' original_name, 0 file_size, 0 parent_id, 1 seq_no) T LEFT JOIN chunk
      on chunk.original_name=T.original_name where chunk.original_name is NULL ;