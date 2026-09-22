const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8082";

export class ApiError extends Error {
  status: number;
  code?: string | undefined;

  constructor(status: number, message: string, code?: string) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...init?.headers,
    },
  });

  if (response.status === 204) {
    return undefined as T;
  }

  const body = await response.json().catch(() => null);

  if (!response.ok) {
    const message = body?.message ?? "Erro inesperado ao comunicar com o servidor";
    throw new ApiError(response.status, message, body?.error);
  }

  return body as T;
}

function withBody(method: string, data?: unknown): RequestInit {
  return data !== undefined ? { method, body: JSON.stringify(data) } : { method };
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, data?: unknown) => request<T>(path, withBody("POST", data)),
  patch: <T>(path: string, data?: unknown) => request<T>(path, withBody("PATCH", data)),
  delete: <T>(path: string) => request<T>(path, { method: "DELETE" }),
};
