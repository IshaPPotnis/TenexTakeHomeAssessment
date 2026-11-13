const logsInput = document.querySelector("#logs-input");
const fileInput = document.querySelector("#log-file");
const parserIdInput = document.querySelector("#parser-id");
const lineCount = document.querySelector("#line-count");
const submitButton = document.querySelector("#submit-button");
const result = document.querySelector("#result");

function getLogs() {
  return logsInput.value.split(/\r?\n/).map((line) => line.trim()).filter(Boolean);
}

function updateLineCount() {
  const count = getLogs().length;
  lineCount.textContent = `${count} ${count === 1 ? "line" : "lines"}`;
}

logsInput.addEventListener("input", updateLineCount);

fileInput.addEventListener("change", async () => {
  const [file] = fileInput.files;
  if (!file) return;

  logsInput.value = await file.text();
  updateLineCount();
  result.hidden = true;
});

submitButton.addEventListener("click", async () => {
  const logs = getLogs();
  if (logs.length === 0) {
    showResult("Add at least one log entry before submitting.", true);
    return;
  }
  const parserId = Number(parserIdInput.value);
  if (!Number.isSafeInteger(parserId) || parserId < 1) {
    showResult("Enter a valid parser ID before submitting.", true);
    return;
  }

  submitButton.disabled = true;
  submitButton.querySelector("span:first-child").textContent = "Processing…";
  showResult("Sending log entries to the processor…", false);

  try {
    const response = await fetch("/api/logs/process", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ logs, parserId })
    });
    const responseBody = await response.text();
    if (!response.ok) {
      throw new Error(responseBody || `Request failed with status ${response.status}`);
    }
    const payload = JSON.parse(responseBody);
    showResult(JSON.stringify(payload, null, 2), false);
  } catch (error) {
    showResult(error.message || "Unable to process logs.", true);
  } finally {
    submitButton.disabled = false;
    submitButton.querySelector("span:first-child").textContent = "Process logs";
  }
});

function showResult(message, isError) {
  result.textContent = message;
  result.classList.toggle("is-error", isError);
  result.hidden = false;
}

updateLineCount();
