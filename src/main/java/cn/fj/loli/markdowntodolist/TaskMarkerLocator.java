package cn.fj.loli.markdowntodolist;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TaskMarkerLocator {
    private static final Pattern TASK_MARKER = Pattern.compile(
            "^[\\t ]*(?:>[\\t ]*)*(?:(?:[-+*])|(?:\\d+[.)]))[\\t ]+\\[([ xX])]"
    );

    private TaskMarkerLocator() {
    }

    static int findMarkerOffset(CharSequence text, int sourceFrom, int sourceTo, int checkboxIndex) {
        List<Integer> markers = collectMarkerOffsets(text);
        if (markers.isEmpty()) {
            return -1;
        }

        int length = text.length();
        boolean hasSourceRange = sourceFrom >= 0 && sourceTo >= sourceFrom && sourceFrom <= length;
        int from = hasSourceRange ? Math.min(sourceFrom, length) : -1;
        int to = hasSourceRange ? Math.min(sourceTo, length) : -1;

        if (hasSourceRange) {
            int sourceLineStart = lineStart(text, from);
            int sourceLineEnd = lineEnd(text, from);
            boolean singleLineRange = to <= sourceLineEnd;
            if (singleLineRange) {
                for (int marker : markers) {
                    if (marker >= sourceLineStart && marker <= sourceLineEnd) {
                        return marker;
                    }
                }
            }

            if (checkboxIndex >= 0 && checkboxIndex < markers.size()) {
                int indexedMarker = markers.get(checkboxIndex);
                int expandedFrom = lineStart(text, from);
                int expandedTo = lineEnd(text, to);
                if (indexedMarker >= expandedFrom && indexedMarker <= expandedTo) {
                    return indexedMarker;
                }
            }

            if (!singleLineRange) {
                for (int marker : markers) {
                    if (marker >= sourceLineStart && marker <= sourceLineEnd) {
                        return marker;
                    }
                }
            }

            int nearest = -1;
            int nearestDistance = Integer.MAX_VALUE;
            for (int marker : markers) {
                if (marker >= from && marker <= to) {
                    int distance = Math.abs(marker - from);
                    if (distance < nearestDistance) {
                        nearest = marker;
                        nearestDistance = distance;
                    }
                }
            }
            if (nearest >= 0) {
                return nearest;
            }
        }

        if (checkboxIndex >= 0 && checkboxIndex < markers.size()) {
            return markers.get(checkboxIndex);
        }
        return -1;
    }

    static List<Integer> collectMarkerOffsets(CharSequence text) {
        List<Integer> result = new ArrayList<>();
        Fence openFence = null;
        int offset = 0;

        while (offset <= text.length()) {
            int end = offset;
            while (end < text.length() && text.charAt(end) != '\n') {
                end++;
            }

            Fence fence = fenceAt(text, offset, end);
            if (openFence != null) {
                if (fence != null && fence.character() == openFence.character()
                        && fence.length() >= openFence.length()) {
                    openFence = null;
                }
            }
            else if (fence != null) {
                openFence = fence;
            }
            else {
                Matcher matcher = TASK_MARKER.matcher(text.subSequence(offset, end));
                if (matcher.find()) {
                    result.add(offset + matcher.start(1));
                }
            }

            if (end >= text.length()) {
                break;
            }
            offset = end + 1;
        }
        return result;
    }

    private static Fence fenceAt(CharSequence text, int start, int end) {
        int index = start;
        int spaces = 0;
        while (index < end && text.charAt(index) == ' ' && spaces < 4) {
            index++;
            spaces++;
        }
        if (spaces > 3 || index >= end) {
            return null;
        }

        char character = text.charAt(index);
        if (character != '`' && character != '~') {
            return null;
        }

        int count = 0;
        while (index < end && text.charAt(index) == character) {
            index++;
            count++;
        }
        return count >= 3 ? new Fence(character, count) : null;
    }

    private static int lineStart(CharSequence text, int offset) {
        int index = Math.min(Math.max(offset, 0), text.length());
        while (index > 0 && text.charAt(index - 1) != '\n') {
            index--;
        }
        return index;
    }

    private static int lineEnd(CharSequence text, int offset) {
        int index = Math.min(Math.max(offset, 0), text.length());
        while (index < text.length() && text.charAt(index) != '\n') {
            index++;
        }
        return index;
    }

    private record Fence(char character, int length) {
    }
}
