const logsInput = document.querySelector("#logs-input");
const fileInput = document.querySelector("#log-file");
const parserSelect = document.querySelector("#parser-id");
const lineCount = document.querySelector("#line-count");
const submitButton = document.querySelector("#submit-button");
const processStatus = document.querySelector("#process-status");
const resultsContainer = document.querySelector("#results");
const processTab = document.querySelector("#process-tab");
const libraryTab = document.querySelector("#library-tab");
const processView = document.querySelector("#process-view");
const libraryView = document.querySelector("#library-view");
const parserCount = document.querySelector("#parser-count");
const parserList = document.querySelector("#parser-list");
const libraryStatus = document.querySelector("#library-status");
const parserForm = document.querySelector("#parser-form");
const formTitle = document.querySelector("#form-title");
const definitionName = document.querySelector("#definition-name");
const definitionType = document.querySelector("#definition-type");
const saveParserButton = document.querySelector("#save-parser-button");

let definitions = [];
let editingParserId = null;

function getLogs() {
  return logsInput.value.split(/\r?\n/).map((line) => line.trim()).filter(Boolean);
}

function updateLineCount() {
  const count = getLogs().length;
  lineCount.textContent = `${count} ${count === 1 ? "line" : "lines"}`;
}

async function apiRequest(path, options = {}) {
  const response = await fetch(path, options);
  const text = await response.text();
  let payload = text;
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      // Keep plain-text API errors readable.
    }
  }
  if (!response.ok) {
    const message = typeof payload === "string"
      ? payload
      : payload.message || payload.error || `Request failed (${response.status})`;
    throw new Error(message);
  }
  return payload;
}

function showStatus(element, message, isError = false) {
  element.textContent = message;
  element.classList.toggle("is-error", isError);
  element.hidden = false;
}

function hideStatus(element) {
  element.hidden = true;
  element.textContent = "";
  element.classList.remove("is-error");
}

function setView(view) {
  const showLibrary = view === "library";
  processView.hidden = showLibrary;
  libraryView.hidden = !showLibrary;
  processTab.classList.toggle("is-active", !showLibrary);
  libraryTab.classList.toggle("is-active", showLibrary);
  processTab.setAttribute("aria-selected", String(!showLibrary));
  libraryTab.setAttribute("aria-selected", String(showLibrary));
  if (showLibrary) loadDefinitions();
}

function renderParserOptions() {
  parserSelect.replaceChildren();
  const prompt = document.createElement("option");
  prompt.value = "";
  prompt.textContent = definitions.length ? "Choose a parser definition" : "No parser definitions available";
  parserSelect.append(prompt);

  for (const definition of definitions) {
    const option = document.createElement("option");
    option.value = definition.parserId;
    option.textContent = `${definition.parserName} · ${definition.parserType} · #${definition.parserId}`;
    parserSelect.append(option);
  }
}

function renderDefinitions() {
  parserList.replaceChildren();
  parserCount.textContent = String(definitions.length);
  renderParserOptions();

  if (definitions.length === 0) {
    const emptyState = document.createElement("p");
    emptyState.className = "empty-state";
    emptyState.textContent = "Your library is empty. Create a parser definition to get started.";
    parserList.append(emptyState);
    return;
  }

  for (const definition of definitions) {
    const card = document.createElement("article");
    card.className = "parser-card";

    const details = document.createElement("div");
    details.className = "parser-details";
    const name = document.createElement("h3");
    name.textContent = definition.parserName;
    const meta = document.createElement("p");
    meta.textContent = `ID ${definition.parserId} · ${definition.parserType}`;
    details.append(name, meta);

    const actions = document.createElement("div");
    actions.className = "parser-actions";
    const editButton = document.createElement("button");
    editButton.type = "button";
    editButton.className = "secondary-button";
    editButton.textContent = "Edit";
    editButton.addEventListener("click", () => beginEdit(definition));
    const deleteButton = document.createElement("button");
    deleteButton.type = "button";
    deleteButton.className = "danger-button";
    deleteButton.textContent = "Delete";
    deleteButton.addEventListener("click", () => deleteDefinition(definition));
    actions.append(editButton, deleteButton);

    card.append(details, actions);
    parserList.append(card);
  }
}

async function loadDefinitions() {
  hideStatus(libraryStatus);
  try {
    const payload = await apiRequest("/api/definitions");
    definitions = Array.isArray(payload) ? payload : [];
    renderDefinitions();
  } catch (error) {
    showStatus(libraryStatus, error.message || "Unable to load parser definitions.", true);
    parserList.replaceChildren();
    definitions = [];
    parserCount.textContent = "0";
    renderParserOptions();
  }
}

function openCreateForm() {
  editingParserId = null;
  parserForm.reset();
  formTitle.textContent = "New parser";
  saveParserButton.querySelector("span").textContent = "Save definition";
  parserForm.hidden = false;
  definitionName.focus();
}

function beginEdit(definition) {
  editingParserId = definition.parserId;
  definitionName.value = definition.parserName;
  definitionType.value = definition.parserType;
  formTitle.textContent = "Edit parser";
  saveParserButton.querySelector("span").textContent = "Update definition";
  parserForm.hidden = false;
  parserForm.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function closeForm() {
  parserForm.hidden = true;
  parserForm.reset();
  editingParserId = null;
}

async function saveDefinition(event) {
  event.preventDefault();
  const definition = {
    parserName: definitionName.value.trim(),
    parserType: definitionType.value
  };
  if (!definition.parserName) {
    showStatus(libraryStatus, "Parser name is required.", true);
    return;
  }

  const isEditing = editingParserId !== null;
  saveParserButton.disabled = true;
  try {
    const savedDefinition = await apiRequest(isEditing ? `/api/definitions/${editingParserId}` : "/api/definitions", {
      method: isEditing ? "PUT" : "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(definition)
    });
    closeForm();
    await loadDefinitions();
    showStatus(libraryStatus, isEditing
      ? `Parser definition ${savedDefinition.parserId} updated.`
      : `Parser definition created with ID ${savedDefinition.parserId}.`);
  } catch (error) {
    showStatus(libraryStatus, error.message || "Unable to save parser definition.", true);
  } finally {
    saveParserButton.disabled = false;
  }
}

async function deleteDefinition(definition) {
  if (!window.confirm(`Delete parser "${definition.parserName}" (ID ${definition.parserId})?`)) return;

  hideStatus(libraryStatus);
  try {
    await apiRequest(`/api/definitions/${definition.parserId}`, { method: "DELETE" });
    if (editingParserId === definition.parserId) closeForm();
    await loadDefinitions();
    showStatus(libraryStatus, "Parser definition deleted.");
  } catch (error) {
    showStatus(libraryStatus, error.message || "Unable to delete parser definition.", true);
  }
}

function renderResults(results) {
  resultsContainer.replaceChildren();
  for (const [index, item] of results.entries()) {
    const card = document.createElement("article");
    card.className = "result-card";

    const heading = document.createElement("div");
    heading.className = "result-card-heading";
    const title = document.createElement("h3");
    title.textContent = `Log ${index + 1}`;
    const note = document.createElement("span");
    note.className = item.parsedOutput == null ? "result-note is-error-text" : "result-note";
    note.textContent = item.notes || (item.parsedOutput == null ? "Parsing failed" : "Parsed");
    heading.append(title, note);

    const originalLabel = document.createElement("p");
    originalLabel.className = "output-label";
    originalLabel.textContent = "INPUT";
    const original = document.createElement("pre");
    original.className = "output-block";
    original.textContent = item.log ?? "";

    const parsedLabel = document.createElement("p");
    parsedLabel.className = "output-label";
    parsedLabel.textContent = "PARSED OUTPUT";
    const parsed = document.createElement("pre");
    parsed.className = "output-block";
    if (item.parsedOutput == null) {
      parsed.textContent = "No parsed output";
      parsed.classList.add("output-placeholder");
    } else {
      try {
        parsed.textContent = JSON.stringify(JSON.parse(item.parsedOutput), null, 2);
      } catch {
        parsed.textContent = item.parsedOutput;
      }
    }

    card.append(heading, originalLabel, original, parsedLabel, parsed);
    resultsContainer.append(card);
  }
}

logsInput.addEventListener("input", updateLineCount);

fileInput.addEventListener("change", async () => {
  const [file] = fileInput.files;
  if (!file) return;
  logsInput.value = await file.text();
  updateLineCount();
  hideStatus(processStatus);
  resultsContainer.replaceChildren();
});

submitButton.addEventListener("click", async () => {
  const logs = getLogs();
  if (logs.length === 0) {
    showStatus(processStatus, "Add at least one log entry before submitting.", true);
    return;
  }
  const parserId = Number(parserSelect.value);
  if (!Number.isSafeInteger(parserId) || parserId < 1) {
    showStatus(processStatus, "Choose a parser definition before submitting.", true);
    return;
  }

  submitButton.disabled = true;
  submitButton.querySelector("span:first-child").textContent = "Processing…";
  hideStatus(processStatus);
  resultsContainer.replaceChildren();
  try {
    const payload = await apiRequest("/api/logs/process", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ logs, parserId })
    });
    if (!Array.isArray(payload.results)) {
      throw new Error("The logs API returned an unexpected response.");
    }
    renderResults(payload.results);
    showStatus(processStatus, `Processed ${payload.results.length} ${payload.results.length === 1 ? "log" : "logs"}.`);
  } catch (error) {
    showStatus(processStatus, error.message || "Unable to process logs.", true);
  } finally {
    submitButton.disabled = false;
    submitButton.querySelector("span:first-child").textContent = "Process logs";
  }
});

processTab.addEventListener("click", () => setView("process"));
libraryTab.addEventListener("click", () => setView("library"));
document.querySelector("#new-parser-button").addEventListener("click", openCreateForm);
document.querySelector("#cancel-edit-button").addEventListener("click", closeForm);
document.querySelector("#cancel-form-button").addEventListener("click", closeForm);
parserForm.addEventListener("submit", saveDefinition);

updateLineCount();
loadDefinitions();
