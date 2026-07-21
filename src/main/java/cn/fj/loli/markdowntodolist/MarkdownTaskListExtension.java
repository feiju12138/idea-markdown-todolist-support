package cn.fj.loli.markdowntodolist;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension;
import org.intellij.plugins.markdown.ui.preview.BrowserPipe;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.intellij.plugins.markdown.ui.preview.ResourceProvider;

import java.util.Collections;
import java.util.List;

final class MarkdownTaskListExtension implements MarkdownBrowserPreviewExtension, ResourceProvider {
    private static final Logger LOG = Logger.getInstance(MarkdownTaskListExtension.class);
    private static final String EVENT_NAME = "markdownTaskListToggle";
    private static final String SCRIPT_NAME = "markdownTaskList/markdown-task-list.js";

    private final MarkdownHtmlPanel panel;
    private final BrowserPipe browserPipe;
    private final BrowserPipe.Handler handler;

    MarkdownTaskListExtension(MarkdownHtmlPanel panel) {
        this.panel = panel;
        this.browserPipe = panel.getBrowserPipe();
        this.handler = new BrowserPipe.Handler() {
            public void messageReceived(String data) {
                processMessage(data);
            }

            public boolean processMessageReceived(String data) {
                return processMessage(data);
            }
        };
        if (browserPipe != null) {
            browserPipe.subscribe(EVENT_NAME, handler);
        }
    }

    @Override
    public Priority getPriority() {
        return Priority.AFTER_ALL;
    }

    @Override
    public List<String> getScripts() {
        return Collections.singletonList(SCRIPT_NAME);
    }

    @Override
    public List<String> getStyles() {
        return Collections.emptyList();
    }

    @Override
    public ResourceProvider getResourceProvider() {
        return this;
    }

    @Override
    public int compareTo(MarkdownBrowserPreviewExtension other) {
        return Integer.compare(other.getPriority().getValue(), getPriority().getValue());
    }

    @Override
    public boolean canProvide(String resourceName) {
        return SCRIPT_NAME.equals(resourceName);
    }

    @Override
    public Resource loadResource(String resourceName) {
        if (!canProvide(resourceName)) {
            return null;
        }
        return ResourceProvider.loadInternalResource(
                MarkdownTaskListExtension.class,
                resourceName,
                "text/javascript"
        );
    }

    private boolean processMessage(String data) {
        ToggleRequest request = ToggleRequest.parse(data);
        if (request == null) {
            LOG.warn("Ignored malformed Markdown task-list message: " + data);
            return false;
        }

        Project project = panel.getProject();
        VirtualFile file = panel.getVirtualFile();
        if (project == null || file == null) {
            return false;
        }

        ApplicationManager.getApplication().invokeLater(() -> applyToggle(project, file, request));
        return false;
    }

    private static void applyToggle(Project project, VirtualFile file, ToggleRequest request) {
        if (project.isDisposed() || !file.isValid() || !file.isWritable()) {
            return;
        }

        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return;
        }

        WriteCommandAction.runWriteCommandAction(
                project,
                "Toggle Markdown task",
                null,
                () -> {
                    int markerOffset = TaskMarkerLocator.findMarkerOffset(
                            document.getCharsSequence(),
                            request.from(),
                            request.to(),
                            request.checkboxIndex()
                    );
                    if (markerOffset < 0 || markerOffset >= document.getTextLength()) {
                        LOG.debug("Could not map a Markdown preview checkbox to its source marker");
                        return;
                    }

                    char desired = request.checked() ? 'x' : ' ';
                    if (document.getCharsSequence().charAt(markerOffset) != desired) {
                        document.replaceString(markerOffset, markerOffset + 1, String.valueOf(desired));
                    }
                }
        );
    }

    @Override
    public void dispose() {
        if (browserPipe != null) {
            browserPipe.removeSubscription(EVENT_NAME, handler);
        }
    }

    private record ToggleRequest(int from, int to, boolean checked, int checkboxIndex) {
        private static ToggleRequest parse(String data) {
            if (data == null) {
                return null;
            }

            String[] parts = data.split("\\|", -1);
            if (parts.length != 4 || !("0".equals(parts[2]) || "1".equals(parts[2]))) {
                return null;
            }

            try {
                return new ToggleRequest(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        "1".equals(parts[2]),
                        Integer.parseInt(parts[3])
                );
            }
            catch (NumberFormatException ignored) {
                return null;
            }
        }
    }
}
