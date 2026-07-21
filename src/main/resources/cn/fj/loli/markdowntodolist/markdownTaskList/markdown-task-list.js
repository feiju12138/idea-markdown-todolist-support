(function() {
  "use strict";

  if (window.__markdownTaskListSupportInstalled) {
    return;
  }
  window.__markdownTaskListSupportInstalled = true;

  const eventName = "markdownTaskListToggle";
  const checkboxSelector = ".task-list-item input[type=\"checkbox\"]";

  function enableTaskCheckboxes(root) {
    const scope = root && root.querySelectorAll ? root : document;
    scope.querySelectorAll(checkboxSelector).forEach(function(checkbox) {
      checkbox.removeAttribute("disabled");
      checkbox.style.cursor = "pointer";
      checkbox.title = "Click to update the Markdown source";
    });

    if (root && root.matches && root.matches(checkboxSelector)) {
      root.removeAttribute("disabled");
      root.style.cursor = "pointer";
      root.title = "Click to update the Markdown source";
    }
  }

  function getSourceRange(checkbox) {
    const meta = document.querySelector('meta[name="markdown-position-attribute-name"]');
    if (!meta || !meta.content) {
      return null;
    }

    const attributeName = meta.content;
    let element = checkbox.closest(".task-list-item");
    while (element && element !== document.body) {
      const value = element.getAttribute(attributeName);
      if (value) {
        const parts = value.split("..");
        if (parts.length === 2) {
          const from = Number.parseInt(parts[0], 10);
          const to = Number.parseInt(parts[1], 10);
          if (Number.isInteger(from) && Number.isInteger(to)) {
            return {from: from, to: to};
          }
        }
      }
      element = element.parentElement;
    }
    return null;
  }

  function checkboxIndex(checkbox) {
    return Array.prototype.indexOf.call(document.querySelectorAll(checkboxSelector), checkbox);
  }

  document.addEventListener("change", function(event) {
    const checkbox = event.target;
    if (!(checkbox instanceof HTMLInputElement) || !checkbox.matches(checkboxSelector)) {
      return;
    }

    const range = getSourceRange(checkbox);
    const from = range ? range.from : -1;
    const to = range ? range.to : -1;
    const index = checkboxIndex(checkbox);
    const data = from + "|" + to + "|" + (checkbox.checked ? "1" : "0") + "|" + index;

    try {
      window.__IntelliJTools.messagePipe.post(eventName, data);
    }
    catch (error) {
      console.error("Failed to update Markdown task checkbox", error);
    }
  }, true);

  const observer = new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
      mutation.addedNodes.forEach(function(node) {
        if (node.nodeType === Node.ELEMENT_NODE) {
          enableTaskCheckboxes(node);
        }
      });
      if (mutation.type === "attributes" && mutation.target.nodeType === Node.ELEMENT_NODE) {
        enableTaskCheckboxes(mutation.target);
      }
    });
  });

  function start() {
    enableTaskCheckboxes(document);
    observer.observe(document.documentElement, {
      subtree: true,
      childList: true,
      attributes: true,
      attributeFilter: ["disabled"]
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", start, {once: true});
  }
  else {
    start();
  }
})();
