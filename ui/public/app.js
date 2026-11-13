const logsInput = document.querySelector("#logs-input");
const fileInput = document.querySelector("#log-file");
const parserSelect = document.querySelector("#parser-id");
const lineCount = document.querySelector("#line-count");
const submitButton = document.querySelector("#submit-button");
const processStatus = document.querySelector("#process-status");
const resultsContainer = document.querySelector("#results");
const processTab = document.querySelector("#process-tab");
const libraryTab = document.querySelector("#library-tab");
const detectionTab = document.querySelector("#detection-tab");
const processView = document.querySelector("#process-view");
const libraryView = document.querySelector("#library-view");
const detectionView = document.querySelector("#detection-view");
const parserCount = document.querySelector("#parser-count");
const parserList = document.querySelector("#parser-list");
const libraryStatus = document.querySelector("#library-status");
const parserForm = document.querySelector("#parser-form");
const formTitle = document.querySelector("#form-title");
const definitionName = document.querySelector("#definition-name");
const definitionType = document.querySelector("#definition-type");
const fieldsToRemoveInput = document.querySelector("#definition-fields-to-remove");
const saveParserButton = document.querySelector("#save-parser-button");
const detectionCount = document.querySelector("#detection-count");
const detectionList = document.querySelector("#detection-list");
const detectionStatus = document.querySelector("#detection-status");
const detectionForm = document.querySelector("#detection-form");
const detectionFormTitle = document.querySelector("#detection-form-title");
const detectionIdHint = document.querySelector("#detection-id-hint");
const detectionName = document.querySelector("#detection-name");
const detectionLogic = document.querySelector("#detection-logic");
const detectionDescription = document.querySelector("#detection-description");
const saveDetectionButton = document.querySelector("#save-detection-button");

let definitions = [];
let detectionRules = [];
let editingParserId = null;
let editingDetectionId = null;

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
  const showProcess = view === "process";
  const showLibrary = view === "library";
  const showDetection = view === "detection";
  processView.hidden = !showProcess;
  libraryView.hidden = !showLibrary;
  detectionView.hidden = !showDetection;
  processTab.classList.toggle("is-active", showProcess);
  libraryTab.classList.toggle("is-active", showLibrary);
  detectionTab.classList.toggle("is-active", showDetection);
  processTab.setAttribute("aria-selected", String(showProcess));
  libraryTab.setAttribute("aria-selected", String(showLibrary));
  detectionTab.setAttribute("aria-selected", String(showDetection));
  if (showLibrary) loadDefinitions();
  if (showDetection) loadDetectionRules();
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

function renderOutputTable(output) {
  const table = document.createElement("table");
  table.className = "output-table";
  const head = document.createElement("thead");
  const headerRow = document.createElement("tr");
  for (const label of ["Field", "Value"]) {
    const header = document.createElement("th");
    header.scope = "col";
    header.textContent = label;
    headerRow.append(header);
  }
  head.append(headerRow);
  table.append(head);

  const body = document.createElement("tbody");
  const rows = [];
  flattenOutput(output, "", rows);
  if (rows.length === 0) rows.push(["(empty)", ""]);
  for (const [field, value] of rows) {
    const row = document.createElement("tr");
    const fieldCell = document.createElement("th");
    fieldCell.scope = "row";
    fieldCell.textContent = field || "Value";
    const valueCell = document.createElement("td");
    valueCell.textContent = value;
    row.append(fieldCell, valueCell);
    body.append(row);
  }
  table.append(body);
  return table;
}

function flattenOutput(value, path, rows) {
  if (Array.isArray(value)) {
    if (value.length === 0) rows.push([path || "Value", "[]"]);
    value.forEach((item, index) => flattenOutput(item, `${path}[${index}]`, rows));
    return;
  }
  if (value !== null && typeof value === "object") {
    const entries = Object.entries(value);
    if (entries.length === 0) rows.push([path || "Value", "{}"]);
    entries.forEach(([key, item]) => flattenOutput(item, path ? `${path}.${key}` : key, rows));
    return;
  }
  rows.push([path || "Value", value === null ? "null" : String(value)]);
}

function renderDetectionRules() {
  detectionList.replaceChildren();
  detectionCount.textContent = String(detectionRules.length);
  if (detectionRules.length === 0) {
    const emptyState = document.createElement("p");
    emptyState.className = "empty-state";
    emptyState.textContent = "Your detection library is empty. Create a rule to get started.";
    detectionList.append(emptyState);
    return;
  }

  for (const rule of detectionRules) {
    const card = document.createElement("article");
    card.className = "parser-card";
    const details = document.createElement("div");
    details.className = "parser-details";
    const name = document.createElement("h3");
    name.textContent = rule.name;
    const meta = document.createElement("p");
    meta.textContent = `ID ${rule.id}`;
    const logic = document.createElement("pre");
    logic.className = "rule-logic";
    logic.textContent = rule.ruleLogic;
    const description = document.createElement("p");
    description.className = "rule-description";
    description.textContent = rule.description;
    details.append(name, meta, logic, description);

    const actions = document.createElement("div");
    actions.className = "parser-actions";
    const editButton = document.createElement("button");
    editButton.type = "button";
    editButton.className = "secondary-button";
    editButton.textContent = "Edit";
    editButton.addEventListener("click", () => beginEditDetection(rule));
    const deleteButton = document.createElement("button");
    deleteButton.type = "button";
    deleteButton.className = "danger-button";
    deleteButton.textContent = "Delete";
    deleteButton.addEventListener("click", () => deleteDetectionRule(rule));
    actions.append(editButton, deleteButton);
    card.append(details, actions);
    detectionList.append(card);
  }
}

async function loadDetectionRules() {
  hideStatus(detectionStatus);
  try {
    const payload = await apiRequest("/api/detection-rules");
    detectionRules = Array.isArray(payload) ? payload : [];
    renderDetectionRules();
  } catch (error) {
    showStatus(detectionStatus, error.message || "Unable to load detection rules.", true);
    detectionList.replaceChildren();
    detectionRules = [];
    detectionCount.textContent = "0";
  }
}

function openCreateDetectionForm() {
  editingDetectionId = null;
  detectionForm.reset();
  detectionFormTitle.textContent = "New detection rule";
  detectionIdHint.textContent = "Rule ID is assigned automatically when the rule is created.";
  saveDetectionButton.querySelector("span").textContent = "Save rule";
  detectionForm.hidden = false;
  detectionName.focus();
}

function beginEditDetection(rule) {
  editingDetectionId = rule.id;
  detectionName.value = rule.name;
  detectionLogic.value = rule.ruleLogic;
  detectionDescription.value = rule.description;
  detectionFormTitle.textContent = "Edit detection rule";
  detectionIdHint.textContent = `Rule ID ${rule.id} cannot be changed.`;
  saveDetectionButton.querySelector("span").textContent = "Update rule";
  detectionForm.hidden = false;
  detectionForm.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function closeDetectionForm() {
  detectionForm.hidden = true;
  detectionForm.reset();
  editingDetectionId = null;
}

async function saveDetectionRule(event) {
  event.preventDefault();
  const rule = {
    name: detectionName.value.trim(),
    ruleLogic: detectionLogic.value.trim(),
    description: detectionDescription.value.trim()
  };
  if (!rule.name || !rule.ruleLogic || !rule.description) {
    showStatus(detectionStatus, "Name, rule logic, and description are required.", true);
    return;
  }

  const isEditing = editingDetectionId !== null;
  saveDetectionButton.disabled = true;
  try {
    const savedRule = await apiRequest(
      isEditing ? `/api/detection-rules/${editingDetectionId}` : "/api/detection-rules",
      {
        method: isEditing ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(rule)
      }
    );
    closeDetectionForm();
    await loadDetectionRules();
    showStatus(detectionStatus, isEditing
      ? `Detection rule ${savedRule.id} updated.`
      : `Detection rule created with ID ${savedRule.id}.`);
  } catch (error) {
    showStatus(detectionStatus, error.message || "Unable to save detection rule.", true);
  } finally {
    saveDetectionButton.disabled = false;
  }
}

async function deleteDetectionRule(rule) {
  if (!window.confirm(`Delete detection rule "${rule.name}" (ID ${rule.id})?`)) return;
  hideStatus(detectionStatus);
  try {
    await apiRequest(`/api/detection-rules/${rule.id}`, { method: "DELETE" });
    if (editingDetectionId === rule.id) closeDetectionForm();
    await loadDetectionRules();
    showStatus(detectionStatus, "Detection rule deleted.");
  } catch (error) {
    showStatus(detectionStatus, error.message || "Unable to delete detection rule.", true);
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
    const exclusions = document.createElement("p");
    const fields = Array.isArray(definition.fieldsToRemove) ? definition.fieldsToRemove : [];
    exclusions.textContent = fields.length ? `Removes: ${fields.join(", ")}` : "No fields removed";
    details.append(name, meta, exclusions);

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
  fieldsToRemoveInput.value = Array.isArray(definition.fieldsToRemove) ? definition.fieldsToRemove.join("\n") : "";
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
    parserType: definitionType.value,
    fieldsToRemove: fieldsToRemoveInput.value.split(/[\n,]/).map((field) => field.trim()).filter(Boolean)
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
    const incidentsLabel = document.createElement("p");
    incidentsLabel.className = "output-label";
    incidentsLabel.textContent = "MATCHED INCIDENTS";
    const incidents = document.createElement("p");
    incidents.className = "matched-incidents";
    const matchedIncidents = Array.isArray(item.matchedIncident) ? item.matchedIncident : [];
    incidents.textContent = matchedIncidents.length ? matchedIncidents.join(", ") : "No matching rules";
    let parsed;
    if (item.parsedOutput == null) {
      parsed = document.createElement("p");
      parsed.className = "output-block output-placeholder";
      parsed.textContent = "No parsed output";
    } else {
      try {
        parsed = renderOutputTable(JSON.parse(item.parsedOutput));
      } catch {
        parsed = document.createElement("pre");
        parsed.className = "output-block";
        parsed.textContent = item.parsedOutput;
      }
    }

    card.append(heading, originalLabel, original, parsedLabel, parsed, incidentsLabel, incidents);
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
detectionTab.addEventListener("click", () => setView("detection"));
document.querySelector("#new-parser-button").addEventListener("click", openCreateForm);
document.querySelector("#cancel-edit-button").addEventListener("click", closeForm);
document.querySelector("#cancel-form-button").addEventListener("click", closeForm);
parserForm.addEventListener("submit", saveDefinition);
document.querySelector("#new-detection-button").addEventListener("click", openCreateDetectionForm);
document.querySelector("#cancel-detection-edit-button").addEventListener("click", closeDetectionForm);
document.querySelector("#cancel-detection-form-button").addEventListener("click", closeDetectionForm);
detectionForm.addEventListener("submit", saveDetectionRule);

updateLineCount();
loadDefinitions();
loadDetectionRules();
