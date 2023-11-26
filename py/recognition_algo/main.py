from libs.readers.body_to_dict import xml_to_dict, json_to_dict
from libs.readers.scan import scan_dict
from libs.middlewares.detect_hash import is_uuid, is_ulid, is_nanoid

json_data = {
    "response": {
        "data": {
            "cars": {
                "red": "3d42d3ca-8ba7-11ee-b9d1-0242ac120002",
                "blue": "машина"
            }
        }
    }
}

print(scan_dict(json_data, is_uuid))