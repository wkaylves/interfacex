package com.kaylves.interfacex.db.storage;

import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.db.model.ScanResultEntity;
import com.kaylves.interfacex.db.model.TagEntity;
import com.intellij.openapi.diagnostic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * XML 文件存储后端实现
 * <p>将数据以 XML 格式存储在项目 .idea/InterfaceX-data.xml 文件中</p>
 * <p>每个方法通过 projectPath 参数定位对应的 XML 文件，无需在构造时指定</p>
 */
@Slf4j
public class XmlStorageBackend implements StorageBackend {

    private static final Logger LOG = Logger.getInstance(XmlStorageBackend.class);
    private static final String DATA_FILE_NAME = "InterfaceX-data.xml";

    private String projectPath;

    public XmlStorageBackend() {
    }

    public XmlStorageBackend(String projectPath) {
        this.projectPath = projectPath;
    }

    private File getDataFile(String path) {
        String effectivePath = (path != null && !path.isEmpty()) ? path : this.projectPath;
        return new File(effectivePath, ".idea/" + DATA_FILE_NAME);
    }

    private Document loadDocument(String path) {
        File file = getDataFile(path);
        if (!file.exists()) {
            return new Document(new Element("interfacex-data"));
        }
        try {
            SAXBuilder builder = new SAXBuilder();
            return builder.build(file);
        } catch (JDOMException | IOException e) {
            LOG.error("Failed to load XML data file", e);
            return new Document(new Element("interfacex-data"));
        }
    }

    private void saveDocument(Document doc, String path) {
        File file = getDataFile(path);
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                outputter.output(doc, fos);
            }
        } catch (IOException e) {
            LOG.error("Failed to save XML data file", e);
        }
    }

    @Override
    public void initialize() {
        if (projectPath != null) {
            File file = getDataFile(projectPath);
            if (!file.exists()) {
                saveDocument(new Document(new Element("interfacex-data")), projectPath);
            }
        }
    }

    @Override
    public void saveScanResults(String projectPath, List<ScanResultEntity> entities) throws SQLException {
        Document doc = loadDocument(projectPath);
        Element root = doc.getRootElement();

        root.removeChildren("scan-results");
        Element scanResultsElement = new Element("scan-results");

        for (ScanResultEntity entity : entities) {
            Element resultElement = new Element("result");
            resultElement.setAttribute("projectPath", nullSafe(entity.getProjectPath()));
            resultElement.setAttribute("moduleName", nullSafe(entity.getModuleName()));
            resultElement.setAttribute("category", nullSafe(entity.getCategory()));
            resultElement.setAttribute("url", nullSafe(entity.getUrl()));
            resultElement.setAttribute("httpMethod", nullSafe(entity.getHttpMethod()));
            resultElement.setAttribute("className", nullSafe(entity.getClassName()));
            resultElement.setAttribute("methodName", nullSafe(entity.getMethodName()));
            resultElement.setAttribute("psiElementHash", String.valueOf(entity.getPsiElementHash() != null ? entity.getPsiElementHash() : 0));
            resultElement.setAttribute("partner", nullSafe(entity.getPartner()));
            resultElement.setAttribute("scanTime", String.valueOf(entity.getScanTime() != null ? entity.getScanTime() : 0));
            scanResultsElement.addContent(resultElement);
        }

        root.addContent(scanResultsElement);
        saveDocument(doc, projectPath);
    }

    @Override
    public List<ScanResultEntity> loadScanResults(String projectPath) throws SQLException {
        Document doc = loadDocument(projectPath);
        Element root = doc.getRootElement();
        Element scanResultsElement = root.getChild("scan-results");
        if (scanResultsElement == null) {
            return Collections.emptyList();
        }

        List<ScanResultEntity> results = new ArrayList<>();
        for (Element resultElement : scanResultsElement.getChildren("result")) {
            ScanResultEntity entity = ScanResultEntity.builder()
                    .projectPath(resultElement.getAttributeValue("projectPath"))
                    .moduleName(resultElement.getAttributeValue("moduleName"))
                    .category(resultElement.getAttributeValue("category"))
                    .url(resultElement.getAttributeValue("url"))
                    .httpMethod(resultElement.getAttributeValue("httpMethod"))
                    .className(resultElement.getAttributeValue("className"))
                    .methodName(resultElement.getAttributeValue("methodName"))
                    .psiElementHash(parseIntSafe(resultElement.getAttributeValue("psiElementHash")))
                    .partner(resultElement.getAttributeValue("partner"))
                    .scanTime(parseLongSafe(resultElement.getAttributeValue("scanTime")))
                    .build();
            results.add(entity);
        }
        return results;
    }

    @Override
    public void deleteScanResults(String projectPath) throws SQLException {
        Document doc = loadDocument(projectPath);
        Element root = doc.getRootElement();
        root.removeChildren("scan-results");
        saveDocument(doc, projectPath);
    }

    @Override
    public void saveTag(TagEntity entity) throws SQLException {
        String path = entity.getProjectPath();
        Document doc = loadDocument(path);
        Element root = doc.getRootElement();

        Element tagsElement = root.getChild("tags");
        if (tagsElement == null) {
            tagsElement = new Element("tags");
            root.addContent(tagsElement);
        }

        Element tagElement = new Element("tag");
        tagElement.setAttribute("projectPath", nullSafe(entity.getProjectPath()));
        tagElement.setAttribute("moduleName", nullSafe(entity.getModuleName()));
        tagElement.setAttribute("category", nullSafe(entity.getCategory()));
        tagElement.setAttribute("url", nullSafe(entity.getUrl()));
        tagElement.setAttribute("httpMethod", nullSafe(entity.getHttpMethod()));
        tagElement.setAttribute("methodName", nullSafe(entity.getMethodName()));
        tagElement.setAttribute("tagName", nullSafe(entity.getTagName()));
        tagElement.setAttribute("tagValue", nullSafe(entity.getTagValue()));
        tagElement.setAttribute("createdTime", String.valueOf(entity.getCreatedTime() != null ? entity.getCreatedTime() : 0));
        tagElement.setAttribute("updatedTime", String.valueOf(entity.getUpdatedTime() != null ? entity.getUpdatedTime() : 0));
        tagsElement.addContent(tagElement);

        saveDocument(doc, path);
    }

    @Override
    public void deleteTag(String projectPath, String moduleName, String category,
                          String url, String httpMethod, String methodName, String tagName) throws SQLException {
        Document doc = loadDocument(projectPath);
        Element root = doc.getRootElement();
        Element tagsElement = root.getChild("tags");
        if (tagsElement == null) {
            return;
        }

        tagsElement.getChildren("tag").removeIf(tagElement ->
                equalsSafe(tagElement.getAttributeValue("projectPath"), projectPath)
                        && equalsSafe(tagElement.getAttributeValue("moduleName"), moduleName)
                        && equalsSafe(tagElement.getAttributeValue("category"), category)
                        && equalsSafe(tagElement.getAttributeValue("url"), url)
                        && equalsSafe(tagElement.getAttributeValue("httpMethod"), httpMethod)
                        && equalsSafe(tagElement.getAttributeValue("methodName"), methodName)
                        && equalsSafe(tagElement.getAttributeValue("tagName"), tagName)
        );

        saveDocument(doc, projectPath);
    }

    @Override
    public List<TagEntity> loadTags(String projectPath) throws SQLException {
        Document doc = loadDocument(projectPath);
        Element root = doc.getRootElement();
        Element tagsElement = root.getChild("tags");
        if (tagsElement == null) {
            return Collections.emptyList();
        }

        List<TagEntity> results = new ArrayList<>();
        for (Element tagElement : tagsElement.getChildren("tag")) {
            if (!equalsSafe(tagElement.getAttributeValue("projectPath"), projectPath)) {
                continue;
            }
            results.add(mapTagElement(tagElement));
        }
        return results;
    }

    @Override
    public List<TagEntity> loadTagsByInterface(String projectPath, String moduleName, String category,
                                                String url, String httpMethod, String methodName) throws SQLException {
        Document doc = loadDocument(projectPath);
        Element root = doc.getRootElement();
        Element tagsElement = root.getChild("tags");
        if (tagsElement == null) {
            return Collections.emptyList();
        }

        List<TagEntity> results = new ArrayList<>();
        for (Element tagElement : tagsElement.getChildren("tag")) {
            if (!equalsSafe(tagElement.getAttributeValue("projectPath"), projectPath)
                    || !equalsSafe(tagElement.getAttributeValue("moduleName"), moduleName)
                    || !equalsSafe(tagElement.getAttributeValue("category"), category)
                    || !equalsSafe(tagElement.getAttributeValue("url"), url)
                    || !equalsSafe(tagElement.getAttributeValue("httpMethod"), httpMethod)
                    || !equalsSafe(tagElement.getAttributeValue("methodName"), methodName)) {
                continue;
            }
            results.add(mapTagElement(tagElement));
        }
        return results;
    }

    @Override
    public List<TagEntity> loadTagsByTagName(String projectPath, String tagName) throws SQLException {
        Document doc = loadDocument(projectPath);
        Element root = doc.getRootElement();
        Element tagsElement = root.getChild("tags");
        if (tagsElement == null) {
            return Collections.emptyList();
        }

        List<TagEntity> results = new ArrayList<>();
        for (Element tagElement : tagsElement.getChildren("tag")) {
            if (!equalsSafe(tagElement.getAttributeValue("projectPath"), projectPath)
                    || !equalsSafe(tagElement.getAttributeValue("tagName"), tagName)) {
                continue;
            }
            results.add(mapTagElement(tagElement));
        }
        return results;
    }

    @Override
    public void saveConfig(ConfigEntity entity) throws SQLException {
        String path = entity.getProjectPath();
        Document doc = loadDocument(path);
        Element root = doc.getRootElement();

        Element configsElement = root.getChild("configs");
        if (configsElement == null) {
            configsElement = new Element("configs");
            root.addContent(configsElement);
        }

        configsElement.getChildren("config").removeIf(configElement ->
                equalsSafe(configElement.getAttributeValue("projectPath"), entity.getProjectPath())
                        && equalsSafe(configElement.getAttributeValue("configKey"), entity.getConfigKey())
        );

        Element configElement = new Element("config");
        configElement.setAttribute("projectPath", nullSafe(entity.getProjectPath()));
        configElement.setAttribute("configKey", nullSafe(entity.getConfigKey()));
        configElement.setAttribute("configValue", nullSafe(entity.getConfigValue()));
        configElement.setAttribute("updatedTime", String.valueOf(entity.getUpdatedTime() != null ? entity.getUpdatedTime() : 0));
        configsElement.addContent(configElement);

        saveDocument(doc, path);
    }

    @Nullable
    @Override
    public String loadConfigValue(String projectPath, String configKey) throws SQLException {
        Document doc = loadDocument(projectPath);
        Element root = doc.getRootElement();
        Element configsElement = root.getChild("configs");
        if (configsElement == null) {
            return null;
        }

        for (Element configElement : configsElement.getChildren("config")) {
            if (equalsSafe(configElement.getAttributeValue("projectPath"), projectPath)
                    && equalsSafe(configElement.getAttributeValue("configKey"), configKey)) {
                return configElement.getAttributeValue("configValue");
            }
        }
        return null;
    }

    @Override
    public List<ConfigEntity> loadConfigs(String projectPath) throws SQLException {
        Document doc = loadDocument(projectPath);
        Element root = doc.getRootElement();
        Element configsElement = root.getChild("configs");
        if (configsElement == null) {
            return Collections.emptyList();
        }

        List<ConfigEntity> results = new ArrayList<>();
        for (Element configElement : configsElement.getChildren("config")) {
            if (!equalsSafe(configElement.getAttributeValue("projectPath"), projectPath)) {
                continue;
            }
            ConfigEntity entity = ConfigEntity.builder()
                    .projectPath(configElement.getAttributeValue("projectPath"))
                    .configKey(configElement.getAttributeValue("configKey"))
                    .configValue(configElement.getAttributeValue("configValue"))
                    .updatedTime(parseLongSafe(configElement.getAttributeValue("updatedTime")))
                    .build();
            results.add(entity);
        }
        return results;
    }

    @Override
    public void saveScanMeta(String projectPath, long scanTime) throws SQLException {
        Document doc = loadDocument(projectPath);
        Element root = doc.getRootElement();

        Element metaElement = root.getChild("scan-meta");
        if (metaElement == null) {
            metaElement = new Element("scan-meta");
            root.addContent(metaElement);
        }

        metaElement.setAttribute("projectPath", nullSafe(projectPath));
        metaElement.setAttribute("lastScanTime", String.valueOf(scanTime));

        saveDocument(doc, projectPath);
    }

    @Nullable
    @Override
    public Long loadLastScanTime(String projectPath) throws SQLException {
        Document doc = loadDocument(projectPath);
        Element root = doc.getRootElement();
        Element metaElement = root.getChild("scan-meta");
        if (metaElement == null) {
            return null;
        }

        if (!equalsSafe(metaElement.getAttributeValue("projectPath"), projectPath)) {
            return null;
        }

        String timeStr = metaElement.getAttributeValue("lastScanTime");
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private TagEntity mapTagElement(Element tagElement) {
        return TagEntity.builder()
                .projectPath(tagElement.getAttributeValue("projectPath"))
                .moduleName(tagElement.getAttributeValue("moduleName"))
                .category(tagElement.getAttributeValue("category"))
                .url(tagElement.getAttributeValue("url"))
                .httpMethod(tagElement.getAttributeValue("httpMethod"))
                .methodName(tagElement.getAttributeValue("methodName"))
                .tagName(tagElement.getAttributeValue("tagName"))
                .tagValue(tagElement.getAttributeValue("tagValue"))
                .createdTime(parseLongSafe(tagElement.getAttributeValue("createdTime")))
                .updatedTime(parseLongSafe(tagElement.getAttributeValue("updatedTime")))
                .build();
    }

    private static String nullSafe(String value) {
        return value != null ? value : "";
    }

    private static boolean equalsSafe(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private static Integer parseIntSafe(String value) {
        if (value == null || value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Long parseLongSafe(String value) {
        if (value == null || value.isEmpty()) return 0L;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
