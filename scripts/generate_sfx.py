#!/usr/bin/env python3
"""
Generate 8 sound effect WAV files using Python stdlib only (wave + math).
Output: sfx_tap, sfx_correct, sfx_incorrect, sfx_reward, sfx_complete,
        sfx_back, sfx_countdown_tick, sfx_countdown_go
Format: 44100 Hz, mono, 16-bit PCM
"""

import wave
import math
import struct
import os

SAMPLE_RATE = 44100
NUM_CHANNELS = 1
SAMPLE_WIDTH = 2  # 16-bit

# ── Output paths ──────────────────────────────────────────────────────────────

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
ROOT_DIR   = os.path.dirname(SCRIPT_DIR)

ANDROID_RAW = os.path.join(ROOT_DIR, "android", "shared", "src", "androidMain", "res", "raw")
IOS_RES     = os.path.join(ROOT_DIR, "ios", "Math", "Resources")

# ── Synthesis helpers ─────────────────────────────────────────────────────────

def make_tone(freq: float, duration: float, volume: float = 0.6,
              vibrato_hz: float = 0.0, vibrato_depth: float = 0.0,
              decay_rate: float = 5.0) -> list[int]:
    """
    Sine wave tone with exponential fade-out.
    vibrato_depth: frequency deviation in Hz for vibrato.
    decay_rate: controls how fast the sound decays (higher = faster).
    """
    n = int(SAMPLE_RATE * duration)
    samples = []
    for i in range(n):
        t = i / SAMPLE_RATE
        if vibrato_hz > 0:
            f = freq + vibrato_depth * math.sin(2 * math.pi * vibrato_hz * t)
        else:
            f = freq
        raw = math.sin(2 * math.pi * f * t)
        fade = math.exp(-decay_rate * t / duration)
        # Short attack (first 5ms) to avoid click
        attack = min(1.0, t / 0.005)
        samples.append(int(raw * fade * attack * volume * 32767))
    return samples


def concat(*parts: list[int]) -> list[int]:
    result = []
    for p in parts:
        result.extend(p)
    return result


def write_wav(path: str, samples: list[int]) -> None:
    with wave.open(path, "w") as wf:
        wf.setnchannels(NUM_CHANNELS)
        wf.setsampwidth(SAMPLE_WIDTH)
        wf.setframerate(SAMPLE_RATE)
        packed = struct.pack(f"<{len(samples)}h", *samples)
        wf.writeframes(packed)
    print(f"  wrote {path}")


def save_both(name: str, samples: list[int]) -> None:
    os.makedirs(ANDROID_RAW, exist_ok=True)
    os.makedirs(IOS_RES, exist_ok=True)
    write_wav(os.path.join(ANDROID_RAW, name), samples)
    write_wav(os.path.join(IOS_RES,     name), samples)


# ── Sound definitions ─────────────────────────────────────────────────────────

def sfx_tap() -> list[int]:
    """800 Hz, 0.12s, fast decay — button click feel."""
    return make_tone(800, 0.12, volume=0.55, decay_rate=7.0)


def sfx_correct() -> list[int]:
    """520 Hz → 780 Hz, each 0.15s — rising / success feel."""
    lo = make_tone(520, 0.15, volume=0.55, decay_rate=4.5)
    hi = make_tone(780, 0.15, volume=0.55, decay_rate=4.5)
    return concat(lo, hi)


def sfx_incorrect() -> list[int]:
    """300 Hz, 0.2s, gentle vibrato — soft wrong-answer tone."""
    return make_tone(300, 0.20, volume=0.45, vibrato_hz=6.0, vibrato_depth=10.0, decay_rate=3.5)


def sfx_reward() -> list[int]:
    """400→520→650→780 Hz arpeggio, each 0.12s — reward feel."""
    return concat(
        make_tone(400, 0.12, volume=0.55, decay_rate=5.0),
        make_tone(520, 0.12, volume=0.55, decay_rate=5.0),
        make_tone(650, 0.12, volume=0.55, decay_rate=5.0),
        make_tone(780, 0.12, volume=0.55, decay_rate=5.0),
    )


def sfx_complete() -> list[int]:
    """520→650→780 Hz each 0.1s + 1040 Hz 0.25s — stage complete fanfare."""
    return concat(
        make_tone(520,  0.10, volume=0.55, decay_rate=5.0),
        make_tone(650,  0.10, volume=0.55, decay_rate=5.0),
        make_tone(780,  0.10, volume=0.55, decay_rate=5.0),
        make_tone(1040, 0.25, volume=0.55, decay_rate=3.5),
    )


def sfx_back() -> list[int]:
    """600→400 Hz descending two-note — 'going back' feel, softer than tap."""
    hi = make_tone(600, 0.09, volume=0.45, decay_rate=6.0)
    lo = make_tone(400, 0.10, volume=0.40, decay_rate=5.0)
    return concat(hi, lo)


def sfx_countdown_tick() -> list[int]:
    """1100 Hz, 0.08s, very fast decay — crisp metronome-like tick for 3/2/1."""
    return make_tone(1100, 0.08, volume=0.60, decay_rate=12.0)


def sfx_countdown_go() -> list[int]:
    """520→780→1040 Hz quick ascending arpeggio — exciting 'Go!' signal."""
    return concat(
        make_tone(520,  0.07, volume=0.60, decay_rate=8.0),
        make_tone(780,  0.07, volume=0.62, decay_rate=7.0),
        make_tone(1040, 0.18, volume=0.65, decay_rate=4.0),
    )


# ── Main ──────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    files = {
        "sfx_tap.wav":            sfx_tap(),
        "sfx_correct.wav":        sfx_correct(),
        "sfx_incorrect.wav":      sfx_incorrect(),
        "sfx_reward.wav":         sfx_reward(),
        "sfx_complete.wav":       sfx_complete(),
        "sfx_back.wav":           sfx_back(),
        "sfx_countdown_tick.wav": sfx_countdown_tick(),
        "sfx_countdown_go.wav":   sfx_countdown_go(),
    }

    print("Generating sound effects...")
    for name, samples in files.items():
        save_both(name, samples)

    total = len(files) * 2
    print(f"\nDone — {total} WAV files written ({len(files)} sounds × 2 platforms).")
    print(f"  Android: {ANDROID_RAW}")
    print(f"  iOS:     {IOS_RES}")
