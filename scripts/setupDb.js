db = db.getSiblingDB('eve');

db.marketItem.createIndex({typeID: 1, regionID:1}, {unique: true});
db.marketItem.createIndex({itemName: 1, regionName:1});
