db = db.getSiblingDB('eve');

db.marketItem.createIndex({typeId: 1, regionId: 1}, {unique: true});
db.marketItem.createIndex({itemName: 1, regionName: 1});
