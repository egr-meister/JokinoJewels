#!/usr/bin/env python3
"""
Jokino Jewels — synthesized sound effects (standard library only).

    python3 tools/generate_sfx.py

Writes app/src/main/res/raw/{click,correct,wrong,match,win,lose}.wav.
Replace them with your own short .wav/.ogg files of the same names at any time.
"""
import math
import os
import struct
import wave

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
RAW = os.path.join(ROOT, "app", "src", "main", "res", "raw")
os.makedirs(RAW, exist_ok=True)

SR = 22050


def tone(freq, dur, vol=0.5, decay=6.0, attack=0.004, harmonics=((1, 1.0),), sweep=0.0, vibrato=0.0):
    n = int(SR * dur)
    out = []
    phase = 0.0
    for i in range(n):
        t = i / SR
        f = freq * (1 + sweep * t / dur) * (1 + vibrato * math.sin(2 * math.pi * 6 * t))
        phase += 2 * math.pi * f / SR
        env = min(1.0, t / attack) * math.exp(-decay * t)
        s = sum(a * math.sin(phase * h) for h, a in harmonics)
        out.append(vol * env * s)
    return out


def seq(*parts, gap=0.0):
    out = []
    for p in parts:
        out.extend(p)
        out.extend([0.0] * int(SR * gap))
    return out


def write_wav(name, samples):
    peak = max(1e-6, max(abs(s) for s in samples))
    scale = min(1.0, 0.9 / peak)
    with wave.open(os.path.join(RAW, name), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(b"".join(struct.pack("<h", int(max(-1, min(1, s * scale)) * 32767)) for s in samples))


def make_sounds():
    bell = ((1, 1.0), (2, 0.35), (3, 0.12))
    write_wav("click.wav", tone(1500, 0.05, 0.6, decay=70, harmonics=((1, 1.0), (2, 0.3))))
    write_wav("correct.wav", seq(tone(880, 0.09, 0.5, 14, harmonics=bell), tone(1318.5, 0.22, 0.55, 9, harmonics=bell)))
    write_wav("wrong.wav", tone(240, 0.32, 0.55, 6, harmonics=((1, 1.0), (3, 0.3), (5, 0.12)), sweep=-0.45))
    write_wav("match.wav", seq(tone(1568, 0.06, 0.45, 20, harmonics=bell), tone(2093, 0.06, 0.45, 20, harmonics=bell),
                              tone(2637, 0.2, 0.5, 10, harmonics=bell)))
    write_wav("win.wav", seq(tone(523.25, 0.12, 0.5, 8, harmonics=bell), tone(659.25, 0.12, 0.5, 8, harmonics=bell),
                            tone(783.99, 0.12, 0.5, 8, harmonics=bell), tone(1046.5, 0.42, 0.6, 4, harmonics=bell)))
    write_wav("lose.wav", seq(tone(392, 0.18, 0.5, 6, harmonics=bell), tone(329.63, 0.18, 0.5, 6, harmonics=bell),
                             tone(261.63, 0.5, 0.5, 3.5, harmonics=bell, vibrato=0.01)))


if __name__ == "__main__":
    make_sounds()
