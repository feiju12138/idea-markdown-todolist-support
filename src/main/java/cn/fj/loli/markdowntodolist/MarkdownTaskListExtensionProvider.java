package cn.fj.loli.markdowntodolist;

import org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;

public final class MarkdownTaskListExtensionProvider implements MarkdownBrowserPreviewExtension.Provider {
    @Override
    public MarkdownBrowserPreviewExtension createBrowserExtension(MarkdownHtmlPanel panel) {
        return new MarkdownTaskListExtension(panel);
    }
}
