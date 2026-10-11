import http from 'node:http';
import fs from 'node:fs/promises';
import path from 'node:path';
import crypto from 'node:crypto';

const PORT = Number(process.env.PORT || 8787);
const KEY = process.env.SUN_CAPE_KEY || '';
const DATA_DIR = path.resolve(process.env.DATA_DIR || './data/capes');
const MAX_BYTES = 128 * 1024;

if (KEY.length < 16) {
  console.error('Set SUN_CAPE_KEY to a private random secret (at least 16 characters).');
  process.exit(1);
}
await fs.mkdir(DATA_DIR, { recursive: true });

function authorized(req) {
  const supplied = req.headers['x-sunclient-key'];
  if (typeof supplied !== 'string') return false;
  const a = Buffer.from(supplied);
  const b = Buffer.from(KEY);
  return a.length === b.length && crypto.timingSafeEqual(a, b);
}

function send(res, status, message, type = 'text/plain; charset=utf-8') {
  res.writeHead(status, { 'Content-Type': type, 'Cache-Control': 'no-store', 'X-Content-Type-Options': 'nosniff' });
  res.end(message);
}

function validUuid(value) {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(value);
}

function validCapePng(bytes) {
  const signature = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);
  return bytes.length >= 24 && bytes.length <= MAX_BYTES
    && bytes.subarray(0, 8).equals(signature)
    && bytes.toString('ascii', 12, 16) === 'IHDR'
    && bytes.readUInt32BE(16) === 64
    && bytes.readUInt32BE(20) === 32;
}

async function readBody(req) {
  const chunks = [];
  let size = 0;
  for await (const chunk of req) {
    size += chunk.length;
    if (size > MAX_BYTES) throw new Error('too-large');
    chunks.push(chunk);
  }
  return Buffer.concat(chunks);
}

const server = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url || '/', 'http://localhost');
    if (req.method === 'GET' && url.pathname === '/health') return send(res, 200, 'Sun Client Cape Server OK');
    if (!authorized(req)) return send(res, 401, 'Invalid Sun Client group key');

    const match = url.pathname.match(/^\/api\/capes\/([0-9a-f-]{36})\.png$/i);
    if (!match || !validUuid(match[1])) return send(res, 404, 'Not found');
    const uuid = match[1].toLowerCase();
    const file = path.join(DATA_DIR, `${uuid}.png`);

    if (req.method === 'GET') {
      try {
        const png = await fs.readFile(file);
        if (!validCapePng(png)) {
          await fs.rm(file, { force: true });
          return send(res, 404, 'Cape is not valid');
        }
        res.writeHead(200, {
          'Content-Type': 'image/png',
          'Content-Length': png.length,
          'Cache-Control': 'no-store',
          'X-Content-Type-Options': 'nosniff'
        });
        return res.end(png);
      } catch (e) {
        if (e.code === 'ENOENT') return send(res, 404, 'No cape');
        throw e;
      }
    }

    if (req.method === 'PUT') {
      const png = await readBody(req);
      if (!validCapePng(png)) return send(res, 400, 'Cape must be a PNG with dimensions 64x32 and max 128 KiB');
      await fs.writeFile(file, png, { mode: 0o600 });
      return send(res, 204, '');
    }

    if (req.method === 'DELETE') {
      await fs.rm(file, { force: true });
      return send(res, 204, '');
    }

    return send(res, 405, 'Method not allowed');
  } catch (e) {
    if (e.message === 'too-large') return send(res, 413, 'Cape too large');
    console.error('Request failed:', e);
    return send(res, 500, 'Internal server error');
  }
});

server.listen(PORT, '0.0.0.0', () => console.log(`Sun Client Cape Server listening on ${PORT}; data: ${DATA_DIR}`));
