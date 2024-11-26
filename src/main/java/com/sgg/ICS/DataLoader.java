package com.sgg.ICS;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;

public class DataLoader {

    private final VectorStore vectorStore;

    private final JdbcClient jdbcClient;

    @Value("classpath:/doc/ics.pdf")
    private Resource pdfResource;

    public DataLoader(VectorStore vectorStore,JdbcClient jdbcClient){
        this.jdbcClient=jdbcClient;
        this.vectorStore=vectorStore;
    }

    @PostConstruct
    public void init(){
        Integer count =
                jdbcClient.sql("select COUNT(*) from vector_store")
                        .query(Integer.class)
                        .single();

        System.out.println("No of Records in the PG Vector Store = " + count);

        if(count == 0) {
            System.out.println("Loading Indian Constitution in the PG Vector Store");
            PdfDocumentReaderConfig config
                    = PdfDocumentReaderConfig.builder()
                    .withPagesPerDocument(1)
                    .build();

            PagePdfDocumentReader reader = new PagePdfDocumentReader(String.valueOf(pdfResource), config);

            var textSplitter = new TokenTextSplitter();
            vectorStore.accept(textSplitter.apply(reader.get()));

            System.out.println("Application is ready to Serve the Requests");
        }
 }
}
