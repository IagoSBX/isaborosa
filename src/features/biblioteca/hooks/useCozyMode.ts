import { useEffect, useState } from "react";

const STORAGE_KEY = "biblioteca-modo-aconchego";

export function useCozyMode() {
  const [enabled, setEnabled] = useState(false);

  useEffect(() => {
    try {
      setEnabled(localStorage.getItem(STORAGE_KEY) === "true");
    } catch {
      // localStorage indisponível (modo privado etc.): mantém o padrão.
    }
  }, []);

  function toggle() {
    setEnabled((current) => {
      const next = !current;
      try {
        localStorage.setItem(STORAGE_KEY, String(next));
      } catch {
        // preferência não persiste nesta sessão, mas a troca ainda funciona.
      }
      return next;
    });
  }

  return { enabled, toggle };
}
