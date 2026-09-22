import { createFileRoute } from "@tanstack/react-router";
import { Maximize2, Pause, Play, X } from "lucide-react";
import { useEffect, useRef, useState } from "react";

import beaumontPhoto from "../assets/beaumont.jpeg";
import busPhoto from "../assets/bus.jpeg";
import espelhoPhoto from "../assets/espelho.jpeg";
import eventoPhoto from "../assets/evento.jpeg";
import evento2Photo from "../assets/evento-2.jpeg";
import objetivoPhoto from "../assets/objetivo.jpeg";
import memory01 from "../assets/memory-01.jpg";
import memory02 from "../assets/memory-02.jpg";
import memory03 from "../assets/memory-03.jpg";
import memory04 from "../assets/memory-04.jpg";
import memory05 from "../assets/memory-05.jpg";
import memory06 from "../assets/memory-06.jpg";
import memory08 from "../assets/memory-08.jpg";
import memory09 from "../assets/memory-09.jpg";
import memory10 from "../assets/memory-10.jpg";
import memory11 from "../assets/memory-11.jpg";

const photos = {
  beaumont: beaumontPhoto,
  bus: busPhoto,
  espelho: espelhoPhoto,
  evento: eventoPhoto,
  evento2: evento2Photo,
  objetivo: objetivoPhoto,
};

const muralPhotos = [
  memory01,
  memory02,
  memory03,
  memory04,
  memory05,
  memory06,
  memory08,
  memory09,
  memory10,
  memory11,
  busPhoto,
  espelhoPhoto,
  objetivoPhoto,
  evento2Photo,
  beaumontPhoto,
];

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Para Isaborosa" },
      { name: "description", content: "Uma coisinha feita pelo Iagostoso, pra Isaborosa." },
      { property: "og:title", content: "Para Isaborosa" },
      { property: "og:description", content: "Uma coisinha feita pelo Iagostoso, pra Isaborosa." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: LoveLetter,
});

function Reveal({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  const ref = useRef<HTMLDivElement>(null);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const node = ref.current;
    if (!node) return;
    const observer = new IntersectionObserver(
      ([entry]) => entry?.isIntersecting && setVisible(true),
      { threshold: 0.14 },
    );
    observer.observe(node);
    return () => observer.disconnect();
  }, []);

  return (
    <div ref={ref} className={`reveal ${visible ? "is-visible" : ""} ${className}`}>
      {children}
    </div>
  );
}

function MusicNote({ expanded = false }: { expanded?: boolean }) {
  const [open, setOpen] = useState(expanded);
  return (
    <div className={`music-note ${open ? "is-playing" : ""}`}>
      <button
        type="button"
        className="music-trigger"
        onClick={() => setOpen((value) => !value)}
        aria-expanded={open}
        aria-label={open ? "Pausar e fechar a playlist" : "Ouvir a playlist escolhida"}
      >
        <span className="music-icon">{open ? <Pause size={15} /> : <Play size={15} />}</span>
        <span>
          <small>Uma música que me lembra você</small>
          <strong>{open ? "besties forever" : "toque para ouvir"}</strong>
        </span>
      </button>
      {open && (
        <iframe
          className="music-embed"
          title="besties forever — playlist"
          src="https://open.spotify.com/embed/playlist/1pTZpk9FYi67VBcEfot6wY?utm_source=generator&theme=0"
          width="100%"
          height="352"
          allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture"
          loading="lazy"
        />
      )}
    </div>
  );
}

function Photo({
  src,
  alt,
  className = "",
  onOpen,
  caption,
}: {
  src: string;
  alt: string;
  className?: string;
  onOpen: () => void;
  caption?: string;
}) {
  return (
    <button type="button" className={`photo ${className}`} onClick={onOpen} aria-label={`Ampliar: ${alt}`}>
      <img src={src} alt={alt} loading="lazy" />
      {caption && <span className="photo-cap">{caption}</span>}
      <span className="photo-expand"><Maximize2 size={15} /></span>
    </button>
  );
}

function LoveLetter() {
  const [selectedPhoto, setSelectedPhoto] = useState<string | null>(null);

  useEffect(() => {
    document.body.style.overflow = selectedPhoto ? "hidden" : "";
    return () => { document.body.style.overflow = ""; };
  }, [selectedPhoto]);

  return (
    <main>
      <section className="opening" aria-label="Para Isaborosa">
        <img className="opening-photo" src={photos.beaumont} alt="Iagostoso e Isaborosa abraçados ao pôr do sol" />
        <div className="opening-shade" />
        <div className="opening-copy">
          <h1>Para Isaborosa</h1>
        </div>
      </section>

      <section id="carta" className="intro paper-section">
        <Reveal className="letter-copy lyrics">
          <p className="lyric">Difícil não lembrar do que nunca se esqueceu</p>
          <p className="lyric">Fácil perceber que seu amor é meu</p>
          <p className="lyric">Difícil não lembrar do que nunca se esqueceu</p>
          <p className="lyric">Fácil perceber que meu amor é seu</p>
          <div className="lyric-verse">
            <p>Eu quero é estar amanhã ao seu lado quando você acordar</p>
            <p>Eu quero estar amanhã sossegado e continuar à te amar</p>
            <p>Eu quero um sonho realizado, uma criança com seu olhar</p>
            <p>Eu quero estar sempre ao seu lado, você me traz paz</p>
            <p className="lyric-yo">Yo!</p>
          </div>
        </Reveal>
      </section>

      <section className="favorite paper-section">
        <Reveal className="favorite-heading">
          <h2>você é o meu lugar<br />quando tudo por um fio está.</h2>
          <p>obrigada por ser meu porto seguro, minha bbzinha.</p>
        </Reveal>
        <Reveal className="feature-photo-wrap">
          <Photo src={photos.evento} alt="Iagostoso e Isaborosa em um abraço" className="feature-photo" onOpen={() => setSelectedPhoto(photos.evento)} />
        </Reveal>
      </section>

      <section className="memories paper-section">
        <Reveal className="memories-title">
          <h2>mural de bobo.</h2>
        </Reveal>
        <div className="photo-wall">
          <Reveal className="wall-a"><Photo src={muralPhotos[0]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[0])} /></Reveal>
          <Reveal className="wall-quote quote-a"><p>simples momentos que valem muito mais.</p></Reveal>
          <Reveal className="wall-b"><Photo src={muralPhotos[1]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[1])} /></Reveal>
          <Reveal className="wall-c"><Photo src={muralPhotos[2]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[2])} /></Reveal>
          <Reveal className="wall-d"><Photo src={muralPhotos[3]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[3])} /></Reveal>
          <Reveal className="wall-e"><Photo src={muralPhotos[4]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[4])} /></Reveal>
          <Reveal className="wall-f"><Photo src={muralPhotos[5]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[5])} /></Reveal>
          <Reveal className="wall-g"><Photo src={muralPhotos[6]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[6])} /></Reveal>
          <Reveal className="wall-h"><Photo src={muralPhotos[7]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[7])} /></Reveal>
          <Reveal className="wall-i"><Photo src={muralPhotos[8]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[8])} /></Reveal>
          <Reveal className="wall-j"><Photo src={muralPhotos[9]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[9])} /></Reveal>
          <Reveal className="wall-k"><Photo src={muralPhotos[10]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[10])} /></Reveal>
          <Reveal className="wall-l"><Photo src={muralPhotos[11]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[11])} /></Reveal>
          <Reveal className="wall-m"><Photo src={muralPhotos[12]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[12])} /></Reveal>
          <Reveal className="wall-n"><Photo src={muralPhotos[13]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[13])} /></Reveal>
          <Reveal className="wall-o"><Photo src={muralPhotos[14]} alt="Iagostoso e Isaborosa, um momento bobo" onOpen={() => setSelectedPhoto(muralPhotos[14])} /></Reveal>
        </div>
      </section>

      <section className="finale">
        <img src={photos.evento2} alt="Iagostoso e Isaborosa abraçados em uma noite com fogos" />
        <div className="finale-shade" />
        <Reveal className="finale-copy finale-final">
          <h2>essa é só o começo<br />da nossa história,<br />amor da minha vida.</h2>
          <div className="finale-music"><MusicNote expanded /></div>
        </Reveal>
      </section>

      {selectedPhoto && (
        <div className="lightbox" role="dialog" aria-modal="true" aria-label="Fotografia ampliada" onClick={() => setSelectedPhoto(null)}>
          <button type="button" className="lightbox-close" onClick={() => setSelectedPhoto(null)} aria-label="Fechar fotografia"><X size={22} /></button>
          <img src={selectedPhoto} alt="Memória de Iagostoso e Isaborosa ampliada" onClick={(event) => event.stopPropagation()} />
        </div>
      )}
    </main>
  );
}
