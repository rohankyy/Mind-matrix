"""
Script to generate the pre-populated nalla_nudi.db SQLite asset.
Uses the EXACT schema from Room's exported schema (version 1).
Identity hash: 3926c72a78f53d3d1a0b12180f8a9487

Run:
  python NallaNudi/scripts/create_db.py
"""

import sqlite3
import os

OUTPUT_PATH = os.path.join(
    os.path.dirname(__file__),
    "..", "app", "src", "main", "assets", "database", "nalla_nudi.db"
)

os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)

if os.path.exists(OUTPUT_PATH):
    os.remove(OUTPUT_PATH)

conn = sqlite3.connect(OUTPUT_PATH)
c = conn.cursor()

# ── Exact Room schema (from AppDatabase_Impl / schema JSON) ──────────────────

c.executescript("""
PRAGMA journal_mode=WAL;

CREATE TABLE IF NOT EXISTS `entry` (
    `id`                   INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `term`                 TEXT NOT NULL,
    `kannada_translation`  TEXT NOT NULL,
    `kannada_explanation`  TEXT NOT NULL,
    `example`              TEXT NOT NULL,
    `subject`              TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS `index_entry_term` ON `entry` (`term`);

CREATE TABLE IF NOT EXISTS `my_list` (
    `id`        INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `entry_id`  INTEGER NOT NULL,
    `saved_at`  INTEGER NOT NULL,
    FOREIGN KEY(`entry_id`) REFERENCES `entry`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS `index_my_list_entry_id` ON `my_list` (`entry_id`);

CREATE TABLE IF NOT EXISTS `word_of_day_history` (
    `id`         INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `entry_id`   INTEGER NOT NULL,
    `date_shown` TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS `index_word_of_day_history_date_shown`
    ON `word_of_day_history` (`date_shown`);

CREATE VIRTUAL TABLE IF NOT EXISTS `entry_fts`
    USING FTS4(`term` TEXT NOT NULL, content=`entry`);

CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_entry_fts_BEFORE_UPDATE
    BEFORE UPDATE ON `entry`
    BEGIN DELETE FROM `entry_fts` WHERE `docid`=OLD.`rowid`; END;

CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_entry_fts_BEFORE_DELETE
    BEFORE DELETE ON `entry`
    BEGIN DELETE FROM `entry_fts` WHERE `docid`=OLD.`rowid`; END;

CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_entry_fts_AFTER_UPDATE
    AFTER UPDATE ON `entry`
    BEGIN INSERT INTO `entry_fts`(`docid`, `term`) VALUES (NEW.`rowid`, NEW.`term`); END;

CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_entry_fts_AFTER_INSERT
    AFTER INSERT ON `entry`
    BEGIN INSERT INTO `entry_fts`(`docid`, `term`) VALUES (NEW.`rowid`, NEW.`term`); END;

CREATE TABLE IF NOT EXISTS room_master_table (
    id            INTEGER PRIMARY KEY,
    identity_hash TEXT
);

INSERT OR REPLACE INTO room_master_table (id, identity_hash)
    VALUES (42, '3926c72a78f53d3d1a0b12180f8a9487');
""")

# ── Glossary Data ─────────────────────────────────────────────────────────────

entries = [
    # SCIENCE
    ("Photosynthesis",    "ದ್ಯುತಿಸಂಶ್ಲೇಷಣೆ",
     "ಸಸ್ಯಗಳು ಸೂರ್ಯನ ಬೆಳಕನ್ನು ಬಳಸಿ ನೀರು ಮತ್ತು ಇಂಗಾಲದ ಡೈಆಕ್ಸೈಡ್‌ನಿಂದ ಆಹಾರ ತಯಾರಿಸುವ ಪ್ರಕ್ರಿಯೆ.",
     "ಮರದ ಎಲೆಗಳು ಸೂರ್ಯನ ಬೆಳಕಿನಲ್ಲಿ ಆಹಾರ ತಯಾರಿಸುತ್ತವೆ.",
     "SCIENCE"),
    ("Gravity",           "ಗುರುತ್ವಾಕರ್ಷಣೆ",
     "ಭೂಮಿಯು ಎಲ್ಲ ವಸ್ತುಗಳನ್ನು ತನ್ನ ಕಡೆಗೆ ಎಳೆಯುವ ಶಕ್ತಿ.",
     "ಮರದಿಂದ ಬಿದ್ದ ಹಣ್ಣು ಕೆಳಗೆ ಬೀಳುವುದು ಗುರುತ್ವಾಕರ್ಷಣೆಯಿಂದ.",
     "SCIENCE"),
    ("Evaporation",       "ಆವಿಯಾಗುವಿಕೆ",
     "ದ್ರವವು ಶಾಖದಿಂದ ಅನಿಲವಾಗಿ ಬದಲಾಗುವ ಪ್ರಕ್ರಿಯೆ.",
     "ಬಿಸಿಲಿನಲ್ಲಿ ನೀರು ಆವಿಯಾಗಿ ಮೋಡವಾಗುತ್ತದೆ.",
     "SCIENCE"),
    ("Osmosis",           "ಅಭಿಸರಣ",
     "ಅರೆ-ಪ್ರವೇಶ್ಯ ಪೊರೆಯ ಮೂಲಕ ನೀರು ಕಡಿಮೆ ಸಾಂದ್ರತೆಯಿಂದ ಹೆಚ್ಚು ಸಾಂದ್ರತೆಯ ಕಡೆಗೆ ಚಲಿಸುವ ಪ್ರಕ್ರಿಯೆ.",
     "ಸಸ್ಯದ ಬೇರುಗಳು ಮಣ್ಣಿನಿಂದ ನೀರನ್ನು ಅಭಿಸರಣದ ಮೂಲಕ ಹೀರಿಕೊಳ್ಳುತ್ತವೆ.",
     "SCIENCE"),
    ("Atom",              "ಪರಮಾಣು",
     "ವಸ್ತುವಿನ ಅತ್ಯಂತ ಚಿಕ್ಕ ಮೂಲ ಘಟಕ, ಇದನ್ನು ರಾಸಾಯನಿಕ ವಿಧಾನದಿಂದ ಒಡೆಯಲಾಗದು.",
     "ಹೈಡ್ರೋಜನ್ ಅನಿಲವು ಎರಡು ಹೈಡ್ರೋಜನ್ ಪರಮಾಣುಗಳಿಂದ ಮಾಡಲ್ಪಟ್ಟಿದೆ.",
     "SCIENCE"),
    ("Friction",          "ಘರ್ಷಣೆ",
     "ಎರಡು ಮೇಲ್ಮೈಗಳು ಒಂದರ ಮೇಲೊಂದು ಉಜ್ಜಿದಾಗ ಉಂಟಾಗುವ ವಿರೋಧ ಶಕ್ತಿ.",
     "ಬ್ರೇಕ್ ಹಾಕಿದಾಗ ಗಾಡಿ ನಿಲ್ಲುವುದು ಘರ್ಷಣೆಯಿಂದ.",
     "SCIENCE"),
    ("Nucleus",           "ಕೇಂದ್ರಕ",
     "ಜೀವಕೋಶದ ನಿಯಂತ್ರಣ ಕೇಂದ್ರ; ಆನುವಂಶಿಕ ಮಾಹಿತಿ (DNA) ಇಲ್ಲಿ ಇರುತ್ತದೆ.",
     "ಜೀವಕೋಶದ ಕೇಂದ್ರಕವು ಮೆದುಳಿನಂತೆ ಕೆಲಸ ಮಾಡುತ್ತದೆ.",
     "SCIENCE"),
    ("Respiration",       "ಉಸಿರಾಟ",
     "ಜೀವಿಗಳು ಆಮ್ಲಜನಕ ಬಳಸಿ ಆಹಾರದಿಂದ ಶಕ್ತಿ ಪಡೆಯುವ ಪ್ರಕ್ರಿಯೆ.",
     "ನಾವು ಉಸಿರಾಡುವಾಗ ಆಮ್ಲಜನಕ ತೆಗೆದುಕೊಂಡು ಇಂಗಾಲದ ಡೈಆಕ್ಸೈಡ್ ಬಿಡುತ್ತೇವೆ.",
     "SCIENCE"),
    ("Magnetism",         "ಕಾಂತೀಯತೆ",
     "ಕಬ್ಬಿಣ ಮತ್ತು ಕೆಲ ಲೋಹಗಳನ್ನು ಆಕರ್ಷಿಸುವ ಶಕ್ತಿ.",
     "ಕಾಂತವು ಕಬ್ಬಿಣದ ಮೊಳೆಗಳನ್ನು ಎಳೆಯುತ್ತದೆ.",
     "SCIENCE"),
    ("Ecosystem",         "ಪರಿಸರ ವ್ಯವಸ್ಥೆ",
     "ಒಂದು ಪ್ರದೇಶದಲ್ಲಿ ಜೀವಿಗಳು ಮತ್ತು ಅವುಗಳ ಭೌತಿಕ ಪರಿಸರ ಒಟ್ಟಾಗಿ ಕಾರ್ಯನಿರ್ವಹಿಸುವ ವ್ಯವಸ್ಥೆ.",
     "ಕಾಡು ಒಂದು ಪರಿಸರ ವ್ಯವಸ್ಥೆ — ಮರಗಳು, ಪ್ರಾಣಿಗಳು, ಮಣ್ಣು ಎಲ್ಲವೂ ಒಟ್ಟಾಗಿ ಬದುಕುತ್ತವೆ.",
     "SCIENCE"),

    # MATHEMATICS
    ("Trigonometry",      "ತ್ರಿಕೋಣಮಿತಿ",
     "ತ್ರಿಭುಜದ ಬಾಹುಗಳು ಮತ್ತು ಕೋನಗಳ ನಡುವಿನ ಸಂಬಂಧವನ್ನು ಅಧ್ಯಯನ ಮಾಡುವ ಗಣಿತ ಶಾಖೆ.",
     "sin, cos, tan ಇವು ತ್ರಿಕೋಣಮಿತಿಯ ಮೂಲ ಅನುಪಾತಗಳು.",
     "MATHEMATICS"),
    ("Algebra",           "ಬೀಜಗಣಿತ",
     "ಸಂಖ್ಯೆಗಳ ಬದಲು ಅಕ್ಷರಗಳನ್ನು ಬಳಸಿ ಸಮೀಕರಣಗಳನ್ನು ಬಿಡಿಸುವ ಗಣಿತ ಶಾಖೆ.",
     "2x + 3 = 7 ಎಂಬ ಸಮೀಕರಣದಲ್ಲಿ x = 2.",
     "MATHEMATICS"),
    ("Probability",       "ಸಂಭಾವ್ಯತೆ",
     "ಒಂದು ಘಟನೆ ಸಂಭವಿಸುವ ಸಾಧ್ಯತೆಯನ್ನು 0 ರಿಂದ 1 ರ ನಡುವಿನ ಸಂಖ್ಯೆಯಿಂದ ವ್ಯಕ್ತಪಡಿಸುವ ಗಣಿತ ಶಾಖೆ.",
     "ನಾಣ್ಯ ಎಸೆದಾಗ ತಲೆ ಬರುವ ಸಂಭಾವ್ಯತೆ 1/2.",
     "MATHEMATICS"),
    ("Geometry",          "ರೇಖಾಗಣಿತ",
     "ಆಕಾರಗಳು, ಗಾತ್ರಗಳು ಮತ್ತು ಅವುಗಳ ಗುಣಲಕ್ಷಣಗಳನ್ನು ಅಧ್ಯಯನ ಮಾಡುವ ಗಣಿತ ಶಾಖೆ.",
     "ವೃತ್ತ, ತ್ರಿಭುಜ, ಚತುರ್ಭುಜ ಇವು ರೇಖಾಗಣಿತದ ಆಕಾರಗಳು.",
     "MATHEMATICS"),
    ("Fraction",          "ಭಿನ್ನರಾಶಿ",
     "ಒಂದು ಪೂರ್ಣ ಸಂಖ್ಯೆಯ ಭಾಗವನ್ನು ಸಂಖ್ಯೆ/ಛೇದ ರೂಪದಲ್ಲಿ ತೋರಿಸುವ ವಿಧಾನ.",
     "ಒಂದು ಕಿತ್ತಳೆಯ ಅರ್ಧ ಭಾಗ = 1/2.",
     "MATHEMATICS"),
    ("Percentage",        "ಶೇಕಡಾ",
     "ಒಂದು ಸಂಖ್ಯೆಯನ್ನು 100 ರ ಭಾಗವಾಗಿ ವ್ಯಕ್ತಪಡಿಸುವ ವಿಧಾನ.",
     "100 ರಲ್ಲಿ 75 ಅಂಕ ಪಡೆದರೆ 75% ಅಂಕ.",
     "MATHEMATICS"),
    ("Integer",           "ಪೂರ್ಣಾಂಕ",
     "ಧನ, ಋಣ ಮತ್ತು ಶೂನ್ಯ ಸಂಖ್ಯೆಗಳನ್ನು ಒಳಗೊಂಡ ಸಂಖ್ಯೆಗಳ ಗುಂಪು.",
     "...-3, -2, -1, 0, 1, 2, 3... ಇವು ಪೂರ್ಣಾಂಕಗಳು.",
     "MATHEMATICS"),
    ("Perimeter",         "ಪರಿಧಿ",
     "ಒಂದು ಆಕಾರದ ಎಲ್ಲ ಬಾಹುಗಳ ಒಟ್ಟು ಉದ್ದ.",
     "4 ಸೆಂ.ಮೀ. ಬಾಹು ಇರುವ ಚೌಕದ ಪರಿಧಿ = 4 x 4 = 16 ಸೆಂ.ಮೀ.",
     "MATHEMATICS"),
    ("Equation",          "ಸಮೀಕರಣ",
     "ಎರಡು ಗಣಿತ ಅಭಿವ್ಯಕ್ತಿಗಳು ಸಮಾನ ಎಂದು ತೋರಿಸುವ ಹೇಳಿಕೆ.",
     "3 + 5 = 8 ಒಂದು ಸರಳ ಸಮೀಕರಣ.",
     "MATHEMATICS"),
    ("Symmetry",          "ಸಮಮಿತಿ",
     "ಒಂದು ಆಕಾರವನ್ನು ಮಧ್ಯದಿಂದ ಮಡಿಸಿದಾಗ ಎರಡೂ ಭಾಗಗಳು ಒಂದೇ ರೀತಿ ಇರುವ ಗುಣ.",
     "ಚಿಟ್ಟೆಯ ರೆಕ್ಕೆಗಳು ಸಮಮಿತಿಯ ಉದಾಹರಣೆ.",
     "MATHEMATICS"),

    # COMMERCE
    ("Inflation",         "ಹಣದುಬ್ಬರ",
     "ಸರಕು ಮತ್ತು ಸೇವೆಗಳ ಬೆಲೆ ಸಾಮಾನ್ಯವಾಗಿ ಏರುತ್ತಾ ಹೋಗುವ ಪ್ರಕ್ರಿಯೆ.",
     "ಕಳೆದ ವರ್ಷ 50 ರೂ. ಇದ್ದ ತರಕಾರಿ ಈ ವರ್ಷ 70 ರೂ. ಆಗಿದ್ದರೆ ಅದು ಹಣದುಬ್ಬರ.",
     "COMMERCE"),
    ("Depreciation",      "ಸವಕಳಿ",
     "ಸ್ಥಿರ ಆಸ್ತಿಯ ಮೌಲ್ಯ ಕಾಲಕ್ರಮೇಣ ಕಡಿಮೆಯಾಗುವ ಪ್ರಕ್ರಿಯೆ.",
     "ಹೊಸ ಕಾರಿನ ಬೆಲೆ ಪ್ರತಿ ವರ್ಷ ಕಡಿಮೆಯಾಗುತ್ತದೆ — ಇದು ಸವಕಳಿ.",
     "COMMERCE"),
    ("Dividend",          "ಲಾಭಾಂಶ",
     "ಕಂಪನಿಯ ಲಾಭದಲ್ಲಿ ಷೇರುದಾರರಿಗೆ ನೀಡಲಾಗುವ ಪಾಲು.",
     "XYZ ಕಂಪನಿ ತನ್ನ ಷೇರುದಾರರಿಗೆ ಪ್ರತಿ ಷೇರಿಗೆ 5 ರೂ. ಲಾಭಾಂಶ ನೀಡಿತು.",
     "COMMERCE"),
    ("Liability",         "ಹೊಣೆಗಾರಿಕೆ",
     "ವ್ಯಕ್ತಿ ಅಥವಾ ಸಂಸ್ಥೆ ಇತರರಿಗೆ ಪಾವತಿಸಬೇಕಾದ ಸಾಲ ಅಥವಾ ಬಾಧ್ಯತೆ.",
     "ಬ್ಯಾಂಕ್ ಸಾಲ ಒಂದು ಹೊಣೆಗಾರಿಕೆ.",
     "COMMERCE"),
    ("Asset",             "ಆಸ್ತಿ",
     "ವ್ಯಕ್ತಿ ಅಥವಾ ಸಂಸ್ಥೆ ಹೊಂದಿರುವ ಮೌಲ್ಯಯುತ ವಸ್ತು ಅಥವಾ ಸಂಪನ್ಮೂಲ.",
     "ಕಟ್ಟಡ, ಯಂತ್ರ, ನಗದು ಇವು ಆಸ್ತಿಗಳು.",
     "COMMERCE"),
    ("Budget",            "ಬಜೆಟ್",
     "ನಿರ್ದಿಷ್ಟ ಅವಧಿಗೆ ಆದಾಯ ಮತ್ತು ವೆಚ್ಚದ ಯೋಜನೆ.",
     "ಸರ್ಕಾರ ಪ್ರತಿ ವರ್ಷ ಬಜೆಟ್ ಮಂಡಿಸಿ ಖರ್ಚು-ವೆಚ್ಚ ನಿರ್ಧರಿಸುತ್ತದೆ.",
     "COMMERCE"),
    ("Revenue",           "ಆದಾಯ",
     "ವ್ಯಾಪಾರ ಚಟುವಟಿಕೆಗಳಿಂದ ಗಳಿಸಿದ ಒಟ್ಟು ಹಣ.",
     "ಅಂಗಡಿಯಲ್ಲಿ ಮಾರಾಟದಿಂದ ಬಂದ ಹಣ ಆದಾಯ.",
     "COMMERCE"),
    ("Profit",            "ಲಾಭ",
     "ಒಟ್ಟು ಆದಾಯದಿಂದ ಒಟ್ಟು ವೆಚ್ಚ ಕಳೆದ ನಂತರ ಉಳಿಯುವ ಹಣ.",
     "1000 ರೂ. ಖರ್ಚು ಮಾಡಿ 1500 ರೂ. ಗಳಿಸಿದರೆ 500 ರೂ. ಲಾಭ.",
     "COMMERCE"),
    ("Tax",               "ತೆರಿಗೆ",
     "ಸರ್ಕಾರ ನಾಗರಿಕರು ಮತ್ತು ವ್ಯಾಪಾರಿಗಳಿಂದ ಕಡ್ಡಾಯವಾಗಿ ಸಂಗ್ರಹಿಸುವ ಹಣ.",
     "ಆದಾಯ ತೆರಿಗೆ, GST ಇವು ತೆರಿಗೆಯ ಉದಾಹರಣೆಗಳು.",
     "COMMERCE"),
    ("Investment",        "ಹೂಡಿಕೆ",
     "ಭವಿಷ್ಯದಲ್ಲಿ ಲಾಭ ಪಡೆಯುವ ಉದ್ದೇಶದಿಂದ ಹಣ ಅಥವಾ ಸಂಪನ್ಮೂಲ ಬಳಸುವ ಕ್ರಿಯೆ.",
     "ಷೇರು ಮಾರುಕಟ್ಟೆಯಲ್ಲಿ ಹಣ ಹಾಕುವುದು ಒಂದು ಹೂಡಿಕೆ.",
     "COMMERCE"),
]

c.executemany(
    """INSERT OR IGNORE INTO entry
       (term, kannada_translation, kannada_explanation, example, subject)
       VALUES (?,?,?,?,?)""",
    entries
)

# Rebuild FTS index to sync with entry table
c.execute("INSERT INTO entry_fts(entry_fts) VALUES('rebuild')")

conn.commit()
conn.close()

print(f"Database created: {os.path.abspath(OUTPUT_PATH)}")
print(f"Total entries: {len(entries)}")
print(f"Identity hash: 3926c72a78f53d3d1a0b12180f8a9487")
