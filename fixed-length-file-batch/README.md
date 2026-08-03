Fixed Length File Spring Batch
==============================


Back and Chunk Size
-------------------

Spring Batch makes the chunk size the JDBC batch size. All items in the chunk need to be on the heap. It's not possible to subdivide a chunk into smaller batches to avoid heap footprint.



