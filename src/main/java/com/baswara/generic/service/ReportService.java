package com.baswara.generic.service;

import com.baswara.generic.config.ApplicationProperties;
import com.baswara.generic.repository.UploadFilesRepository;
import com.mongodb.DBObject;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

@Service
@Transactional
public class ReportService {

    private final ApplicationProperties applicationProperties;

    private final Logger log = LoggerFactory.getLogger(ReportService.class);

    public ReportService(ApplicationProperties applicationProperties, UploadFilesRepository uploadFilesRepository) {
        this.applicationProperties = applicationProperties;
    }

    public byte[] generate(String id, Map<String, Object> param, String format) throws IOException, JRException {
        File reportFile = new File(applicationProperties.getReportFolder() + id + ".jasper");
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportFile);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, param);
        byte[] reportBytes = export(jasperPrint, format);
        return reportBytes;
    }

    public byte[] export(final JasperPrint print, String format) throws JRException {
        final Exporter exporter;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean html = false;

        switch (format) {
            case "html":
                exporter = new HtmlExporter();
                exporter.setExporterOutput(new SimpleHtmlExporterOutput(out));
                html = true;
                break;

            case "csv":
                exporter = new JRCsvExporter();
                break;

            case "xml":
                exporter = new JRXmlExporter();
                break;

            case "xlsx":
                exporter = new JRXlsxExporter();
                break;

            case "pdf":
                exporter = new JRPdfExporter();
                break;

            default:
                throw new JRException("Unknown report format: " + format);
        }

        if (!html) {
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        }

        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.exportReport();

        return out.toByteArray();
    }

}
