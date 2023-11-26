import json
import xmltodict

def xml_to_dict(XML: str) -> dict:
    return xmltodict.parse(XML)

def json_to_dict(JSON: str) -> dict:
    return json.loads(JSON)