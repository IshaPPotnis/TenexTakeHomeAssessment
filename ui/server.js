const http = require("node:http");
const fs = require("node:fs");
const path = require("node:path");

const port = Number(process.env.PORT || 3000);
const apiBaseUrl = (process.env.API_BASE_URL || "http://localhost:8080").replace(/\/$/, "");
const publicDirectory = path.join(__dirname, "public");
const contentTypes = {
  ".css": "text/css; charset=utf-8",
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8"
};

const server = http.createServer(async (request, response) => {
  if (request.url === "/api/logs/process") {
    if (request.method !== "POST") {
      response.writeHead(405, { Allow: "POST", "Content-Type": "text/plain; charset=utf-8" });
      response.end("Method not allowed");
      return;
    }

    try {
      const body = await readRequestBody(request);
      const apiResponse = await fetch(`${apiBaseUrl}/api/logs/process`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body
      });
      const responseBody = await apiResponse.text();
      response.writeHead(apiResponse.status, {
        "Content-Type": apiResponse.headers.get("content-type") || "text/plain; charset=utf-8"
      });
      response.end(responseBody);
    } catch (error) {
      const status = error instanceof RangeError ? 413 : 502;
      response.writeHead(status, { "Content-Type": "text/plain; charset=utf-8" });
      response.end(status === 413 ? error.message : "Unable to reach the logs API");
    }
    return;
  }

  if (request.method !== "GET" && request.method !== "HEAD") {
    response.writeHead(405, { Allow: "GET, HEAD", "Content-Type": "text/plain; charset=utf-8" });
    response.end("Method not allowed");
    return;
  }

  const pathname = new URL(request.url, "http://localhost").pathname;
  const requestedPath = pathname === "/" ? "index.html" : decodeURIComponent(pathname.slice(1));
  const filePath = path.resolve(publicDirectory, requestedPath);
  if (!filePath.startsWith(`${publicDirectory}${path.sep}`)) {
    response.writeHead(404);
    response.end();
    return;
  }

  fs.readFile(filePath, (error, file) => {
    if (error) {
      response.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
      response.end("Not found");
      return;
    }

    response.writeHead(200, {
      "Content-Type": contentTypes[path.extname(filePath)] || "application/octet-stream"
    });
    response.end(request.method === "HEAD" ? undefined : file);
  });
});

async function readRequestBody(request) {
  const chunks = [];
  let length = 0;
  for await (const chunk of request) {
    length += chunk.length;
    if (length > 1024 * 1024) {
      throw new RangeError("Request body exceeds the 1 MB limit");
    }
    chunks.push(chunk);
  }
  return Buffer.concat(chunks).toString("utf8");
}

server.listen(port, "0.0.0.0", () => {
  console.log(`Logs UI listening on port ${port}; API: ${apiBaseUrl}`);
});
