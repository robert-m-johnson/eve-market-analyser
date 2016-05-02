# Extracts information from an EVE type data YAML export into a CSV
# Obtain typeIDs.yaml from somewhere like https://www.fuzzwork.co.uk/dump/
import yaml

with open("/home/robertjohnson/Downloads/eve-data/typeIDs.yaml", 'r') as input_file:
    document = yaml.load(input_file)
    with open("typeIDs.csv", "w") as output_file:
        type_ids = list(document.keys())
        type_ids.sort();
        for type_id in type_ids:
            try:
                type_doc = document.get(type_id)
                name_doc = type_doc.get('name')

                if name_doc == None:
                    print('Found null name:' + str(name_doc))
                else:
                    name = name_doc.get('en')
                    if name == None:
                        print('Name doc has no English translation: ' + str(type_doc))
                    else:
                        line = str(type_id) + '|' + name + '\n'
                        output_file.write(line)
            except Exception as e:
                print('Error processing doc: ' + str(type_doc) + str(e))
print('Finished')
