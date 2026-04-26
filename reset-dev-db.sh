#!/bin/bash
# reset-dev-db.sh
# Drop semua collections di database neurixa (dev only).
# Setelah ini, jalankan ./gradlew :neurixa-boot:bootRun
# dan DevDataSeeder akan otomatis seed users default.

DB="neurixa"
MONGO_URI="mongodb://localhost:27017"

echo "⚠️  Resetting dev database: $DB"
echo "Collections yang akan di-drop: users, files, folders, file_versions, articles, categories, tags, comments"
echo ""
read -p "Lanjutkan? (y/N): " confirm
[[ "$confirm" != "y" && "$confirm" != "Y" ]] && echo "Dibatalkan." && exit 0

mongosh "$MONGO_URI/$DB" --quiet --eval "
  ['users','files','folders','file_versions','articles','categories','tags','comments']
    .forEach(c => {
      try { db.getCollection(c).drop(); print('Dropped: ' + c); }
      catch(e) { print('Skip: ' + c); }
    });
  print('Done. Jalankan: ./gradlew :neurixa-boot:bootRun');
"
