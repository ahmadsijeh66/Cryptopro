"""
Crypto Technical Analysis Tool
Strategies: Support & Resistance, Moving Average 100, RSI, MACD, Bollinger Bands
"""

import json
import math
from datetime import datetime, timedelta
import random


# ─────────────────────────────────────────────
# 1. DATA LAYER  (replace fetch_ohlcv with a
#    real exchange call, e.g. ccxt / Binance API)
# ─────────────────────────────────────────────

def fetch_ohlcv(symbol: str = "BTC/USDT", days: int = 200) -> list[dict]:
    """
    Simulates OHLCV data.
    Replace the body with:
        import ccxt
        exchange = ccxt.binance()
        raw = exchange.fetch_ohlcv(symbol, '1d', limit=days)
        return [{"date": datetime.fromtimestamp(r[0]/1000).strftime('%Y-%m-%d'),
                 "open": r[1], "high": r[2], "low": r[3],
                 "close": r[4], "volume": r[5]} for r in raw]
    """
    random.seed(42)
    base = 40_000.0
    candles = []
    date = datetime.now() - timedelta(days=days)
    for _ in range(days):
        change = random.uniform(-0.04, 0.04)
        open_  = base
        close  = round(base * (1 + change), 2)
        high   = round(max(open_, close) * (1 + random.uniform(0, 0.02)), 2)
        low    = round(min(open_, close) * (1 - random.uniform(0, 0.02)), 2)
        volume = round(random.uniform(10_000, 50_000), 2)
        candles.append({
            "date": date.strftime("%Y-%m-%d"),
            "open": open_,
            "high": high,
            "low": low,
            "close": close,
            "volume": volume,
        })
        base = close
        date += timedelta(days=1)
    return candles


# ─────────────────────────────────────────────
# 2. INDICATOR CALCULATIONS
# ─────────────────────────────────────────────

def moving_average(closes: list[float], period: int) -> list[float | None]:
    result = [None] * (period - 1)
    for i in range(period - 1, len(closes)):
        result.append(round(sum(closes[i - period + 1 : i + 1]) / period, 2))
    return result


def ema(closes: list[float], period: int) -> list[float | None]:
    result = [None] * (period - 1)
    k = 2 / (period + 1)
    seed = sum(closes[:period]) / period
    result.append(round(seed, 2))
    for price in closes[period:]:
        seed = price * k + seed * (1 - k)
        result.append(round(seed, 2))
    return result


def rsi(closes: list[float], period: int = 14) -> list[float | None]:
    result = [None] * period
    gains, losses = [], []
    for i in range(1, period + 1):
        diff = closes[i] - closes[i - 1]
        gains.append(max(diff, 0))
        losses.append(max(-diff, 0))
    avg_gain = sum(gains) / period
    avg_loss = sum(losses) / period
    for i in range(period, len(closes) - 1):
        diff = closes[i + 1] - closes[i]
        avg_gain = (avg_gain * (period - 1) + max(diff, 0)) / period
        avg_loss = (avg_loss * (period - 1) + max(-diff, 0)) / period
        rs = avg_gain / avg_loss if avg_loss else float("inf")
        result.append(round(100 - 100 / (1 + rs), 2))
    return result


def macd(closes: list[float],
         fast: int = 12, slow: int = 26, signal: int = 9
         ) -> tuple[list, list, list]:
    ema_fast   = ema(closes, fast)
    ema_slow   = ema(closes, slow)
    macd_line  = [
        round(f - s, 2) if f is not None and s is not None else None
        for f, s in zip(ema_fast, ema_slow)
    ]
    valid      = [v for v in macd_line if v is not None]
    sig_raw    = ema(valid, signal)
    offset     = len(macd_line) - len(valid)
    sig_line   = [None] * (offset + signal - 1) + sig_raw[signal - 1:]
    histogram  = [
        round(m - s, 2) if m is not None and s is not None else None
        for m, s in zip(macd_line, sig_line)
    ]
    return macd_line, sig_line, histogram


def bollinger_bands(closes: list[float],
                    period: int = 20, std_mult: float = 2.0
                    ) -> tuple[list, list, list]:
    ma     = moving_average(closes, period)
    upper, lower = [], []
    for i, avg in enumerate(ma):
        if avg is None:
            upper.append(None); lower.append(None)
        else:
            window = closes[i - period + 1 : i + 1]
            variance = sum((p - avg) ** 2 for p in window) / period
            sd = math.sqrt(variance)
            upper.append(round(avg + std_mult * sd, 2))
            lower.append(round(avg - std_mult * sd, 2))
    return ma, upper, lower


def find_support_resistance(candles: list[dict],
                             lookback: int = 20,
                             tolerance: float = 0.02
                             ) -> tuple[list[float], list[float]]:
    """
    Detect pivot highs (resistance) and pivot lows (support)
    within a rolling window, then cluster nearby levels.
    """
    highs  = [c["high"]  for c in candles]
    lows   = [c["low"]   for c in candles]
    n = len(candles)

    pivot_highs, pivot_lows = [], []
    half = lookback // 2
    for i in range(half, n - half):
        window_h = highs[i - half : i + half + 1]
        window_l = lows[i - half  : i + half + 1]
        if highs[i] == max(window_h):
            pivot_highs.append(highs[i])
        if lows[i] == min(window_l):
            pivot_lows.append(lows[i])

    def cluster(levels: list[float]) -> list[float]:
        if not levels:
            return []
        levels = sorted(levels)
        clusters, group = [], [levels[0]]
        for lvl in levels[1:]:
            if (lvl - group[0]) / group[0] <= tolerance:
                group.append(lvl)
            else:
                clusters.append(round(sum(group) / len(group), 2))
                group = [lvl]
        clusters.append(round(sum(group) / len(group), 2))
        return clusters

    return cluster(pivot_lows), cluster(pivot_highs)


# ─────────────────────────────────────────────
# 3. STRATEGY SIGNALS
# ─────────────────────────────────────────────

def strategy_ma100(closes: list[float],
                   ma100: list[float | None]) -> list[str]:
    """
    BUY  when price crosses above MA-100
    SELL when price crosses below MA-100
    """
    signals = ["HOLD"] * len(closes)
    for i in range(1, len(closes)):
        if ma100[i] is None or ma100[i - 1] is None:
            continue
        if closes[i - 1] < ma100[i - 1] and closes[i] >= ma100[i]:
            signals[i] = "BUY"
        elif closes[i - 1] > ma100[i - 1] and closes[i] <= ma100[i]:
            signals[i] = "SELL"
    return signals


def strategy_rsi(rsi_vals: list[float | None],
                 oversold: float = 30,
                 overbought: float = 70) -> list[str]:
    """
    BUY  when RSI crosses above oversold (30)
    SELL when RSI crosses below overbought (70)
    """
    signals = ["HOLD"] * len(rsi_vals)
    for i in range(1, len(rsi_vals)):
        r, r_prev = rsi_vals[i], rsi_vals[i - 1]
        if r is None or r_prev is None:
            continue
        if r_prev <= oversold and r > oversold:
            signals[i] = "BUY"
        elif r_prev >= overbought and r < overbought:
            signals[i] = "SELL"
    return signals


def strategy_macd_crossover(macd_line: list, sig_line: list) -> list[str]:
    """
    BUY  when MACD crosses above signal
    SELL when MACD crosses below signal
    """
    signals = ["HOLD"] * len(macd_line)
    for i in range(1, len(macd_line)):
        m, s = macd_line[i], sig_line[i]
        mp, sp = macd_line[i - 1], sig_line[i - 1]
        if None in (m, s, mp, sp):
            continue
        if mp < sp and m >= s:
            signals[i] = "BUY"
        elif mp > sp and m <= s:
            signals[i] = "SELL"
    return signals


def strategy_bollinger(closes: list[float],
                        upper: list, lower: list) -> list[str]:
    """
    BUY  when price touches/crosses lower band (oversold squeeze)
    SELL when price touches/crosses upper band (overbought squeeze)
    """
    signals = ["HOLD"] * len(closes)
    for i in range(len(closes)):
        if upper[i] is None or lower[i] is None:
            continue
        if closes[i] <= lower[i]:
            signals[i] = "BUY"
        elif closes[i] >= upper[i]:
            signals[i] = "SELL"
    return signals


def strategy_support_resistance(closes: list[float],
                                  support_levels: list[float],
                                  resistance_levels: list[float],
                                  tolerance: float = 0.015) -> list[str]:
    """
    BUY  when price bounces off a support level
    SELL when price is rejected at a resistance level
    """
    signals = ["HOLD"] * len(closes)
    for i, price in enumerate(closes):
        for s in support_levels:
            if abs(price - s) / s <= tolerance:
                signals[i] = "BUY"
                break
        if signals[i] == "HOLD":
            for r in resistance_levels:
                if abs(price - r) / r <= tolerance:
                    signals[i] = "SELL"
                    break
    return signals


# ─────────────────────────────────────────────
# 4. COMBINED CONSENSUS SIGNAL
# ─────────────────────────────────────────────

def consensus_signal(*signal_lists) -> list[str]:
    """
    Majority-vote across all strategies.
    Requires > half strategies to agree for a non-HOLD.
    """
    n = min(len(sl) for sl in signal_lists)
    result = []
    for i in range(n):
        votes = [sl[i] for sl in signal_lists]
        buys  = votes.count("BUY")
        sells = votes.count("SELL")
        threshold = len(signal_lists) / 2
        if buys > threshold:
            result.append("BUY")
        elif sells > threshold:
            result.append("SELL")
        else:
            result.append("HOLD")
    return result


# ─────────────────────────────────────────────
# 5. BACK-TEST
# ─────────────────────────────────────────────

def backtest(candles: list[dict],
             signals: list[str],
             initial_capital: float = 10_000.0) -> dict:
    capital = initial_capital
    position = 0.0          # BTC held
    trades   = []
    equity   = []

    for i, (candle, signal) in enumerate(zip(candles, signals)):
        price = candle["close"]
        if signal == "BUY" and capital > 0:
            position = capital / price
            capital  = 0.0
            trades.append({"type": "BUY",  "date": candle["date"],
                            "price": price, "qty": round(position, 6)})
        elif signal == "SELL" and position > 0:
            capital  = position * price
            trades.append({"type": "SELL", "date": candle["date"],
                            "price": price, "pnl": round(capital - initial_capital, 2)})
            position = 0.0
        equity.append(round(capital + position * price, 2))

    final_value  = capital + position * candles[-1]["close"]
    total_return = (final_value - initial_capital) / initial_capital * 100
    wins         = [t for t in trades if t.get("pnl", 0) > 0]
    win_rate     = len(wins) / max(len([t for t in trades if t["type"] == "SELL"]), 1) * 100

    return {
        "initial_capital": initial_capital,
        "final_value":     round(final_value, 2),
        "total_return_pct": round(total_return, 2),
        "total_trades":    len(trades),
        "win_rate_pct":    round(win_rate, 2),
        "trades":          trades,
        "equity_curve":    equity,
    }


# ─────────────────────────────────────────────
# 6. REPORT
# ─────────────────────────────────────────────

def print_report(symbol: str,
                 candles: list[dict],
                 support: list[float],
                 resistance: list[float],
                 ma100: list,
                 rsi_vals: list,
                 macd_line: list,
                 sig_line: list,
                 individual_signals: dict[str, list[str]],
                 consensus: list[str],
                 bt: dict) -> None:

    latest        = candles[-1]
    latest_rsi    = next((v for v in reversed(rsi_vals)    if v is not None), None)
    latest_macd   = next((v for v in reversed(macd_line)   if v is not None), None)
    latest_signal = next((v for v in reversed(sig_line)    if v is not None), None)
    latest_ma100  = next((v for v in reversed(ma100)       if v is not None), None)

    sep = "═" * 60

    print(f"\n{sep}")
    print(f"  CRYPTO ANALYSIS REPORT  ·  {symbol}")
    print(f"  Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(sep)

    print(f"\n{'─'*60}")
    print("  LATEST MARKET SNAPSHOT")
    print(f"{'─'*60}")
    print(f"  Date        : {latest['date']}")
    print(f"  Close       : ${latest['close']:>12,.2f}")
    print(f"  High / Low  : ${latest['high']:>12,.2f}  /  ${latest['low']:>12,.2f}")
    print(f"  Volume      : {latest['volume']:>12,.2f}")

    print(f"\n{'─'*60}")
    print("  INDICATORS")
    print(f"{'─'*60}")
    print(f"  MA-100      : ${latest_ma100:>12,.2f}" if latest_ma100 else "  MA-100     : N/A")
    print(f"  RSI-14      : {latest_rsi:>12.2f}" if latest_rsi else "  RSI-14     : N/A")
    print(f"  MACD Line   : {latest_macd:>12.4f}" if latest_macd is not None else "  MACD Line  : N/A")
    print(f"  MACD Signal : {latest_signal:>12.4f}" if latest_signal is not None else "  MACD Signal: N/A")

    print(f"\n{'─'*60}")
    print("  SUPPORT & RESISTANCE LEVELS")
    print(f"{'─'*60}")
    print(f"  Support    : {', '.join(f'${v:,.0f}' for v in support[-5:]) or 'None found'}")
    print(f"  Resistance : {', '.join(f'${v:,.0f}' for v in resistance[-5:]) or 'None found'}")

    print(f"\n{'─'*60}")
    print("  STRATEGY SIGNALS  (last 5 candles)")
    print(f"{'─'*60}")
    dates = [c["date"] for c in candles[-5:]]
    header = f"  {'Strategy':<22}" + "".join(f"  {d[5:]}" for d in dates)
    print(header)
    for name, sigs in individual_signals.items():
        row = f"  {name:<22}" + "".join(f"  {'🟢BUY' if s == 'BUY' else '🔴SEL' if s == 'SELL' else '⬜HLD':>7}" for s in sigs[-5:])
        print(row)
    con_row = f"  {'CONSENSUS':<22}" + "".join(
        f"  {'🟢BUY' if s == 'BUY' else '🔴SEL' if s == 'SELL' else '⬜HLD':>7}" for s in consensus[-5:]
    )
    print(f"{'─'*60}")
    print(con_row)

    print(f"\n{'─'*60}")
    print("  BACK-TEST  (consensus signal, $10 000 start)")
    print(f"{'─'*60}")
    print(f"  Final Value     : ${bt['final_value']:>12,.2f}")
    print(f"  Total Return    : {bt['total_return_pct']:>+12.2f} %")
    print(f"  Total Trades    : {bt['total_trades']:>12}")
    print(f"  Win Rate        : {bt['win_rate_pct']:>12.2f} %")

    if bt["trades"]:
        print(f"\n  {'─'*55}")
        print(f"  {'Date':<12}  {'Type':<5}  {'Price':>12}  {'PnL':>10}")
        print(f"  {'─'*55}")
        for t in bt["trades"][-10:]:
            pnl_str = f"${t['pnl']:>+10,.2f}" if "pnl" in t else f"{'':>11}"
            print(f"  {t['date']:<12}  {t['type']:<5}  ${t['price']:>11,.2f}  {pnl_str}")

    print(f"\n{sep}\n")


# ─────────────────────────────────────────────
# 7. MAIN
# ─────────────────────────────────────────────

def run_analysis(symbol: str = "BTC/USDT", days: int = 200) -> dict:
    print(f"[*] Fetching {days} days of OHLCV data for {symbol} …")
    candles = fetch_ohlcv(symbol, days)
    closes  = [c["close"] for c in candles]

    # — Indicators —
    print("[*] Computing indicators …")
    ma100            = moving_average(closes, 100)
    rsi_vals         = rsi(closes, 14)
    macd_line, sig_line, histogram = macd(closes, 12, 26, 9)
    bb_mid, bb_upper, bb_lower     = bollinger_bands(closes, 20, 2.0)
    support, resistance             = find_support_resistance(candles, 20)

    # — Strategy signals —
    print("[*] Generating strategy signals …")
    sig_ma100 = strategy_ma100(closes, ma100)
    sig_rsi   = strategy_rsi(rsi_vals)
    sig_macd  = strategy_macd_crossover(macd_line, sig_line)
    sig_bb    = strategy_bollinger(closes, bb_upper, bb_lower)
    sig_sr    = strategy_support_resistance(closes, support, resistance)

    individual = {
        "MA-100 Crossover":   sig_ma100,
        "RSI (30/70)":        sig_rsi,
        "MACD Crossover":     sig_macd,
        "Bollinger Bands":    sig_bb,
        "Support/Resistance": sig_sr,
    }
    consensus = consensus_signal(sig_ma100, sig_rsi, sig_macd, sig_bb, sig_sr)

    # — Back-test —
    print("[*] Running back-test …")
    bt = backtest(candles, consensus)

    # — Report —
    print_report(symbol, candles, support, resistance,
                 ma100, rsi_vals, macd_line, sig_line,
                 individual, consensus, bt)

    return {
        "candles":    candles,
        "indicators": {
            "ma100": ma100,
            "rsi":   rsi_vals,
            "macd":  macd_line,
            "signal_line": sig_line,
            "histogram":   histogram,
            "bb_upper":    bb_upper,
            "bb_mid":      bb_mid,
            "bb_lower":    bb_lower,
        },
        "levels": {"support": support, "resistance": resistance},
        "signals": {**individual, "consensus": consensus},
        "backtest": bt,
    }


if __name__ == "__main__":
    result = run_analysis("BTC/USDT", days=200)

    # Optional: dump full signal table to JSON
    with open("analysis_output.json", "w") as f:
        payload = {
            "symbol":   "BTC/USDT",
            "levels":   result["levels"],
            "backtest": result["backtest"],
        }
        json.dump(payload, f, indent=2)
    print("[*] analysis_output.json written.")
