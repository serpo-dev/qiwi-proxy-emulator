import re


def is_uuid(string: str) -> bool:
    pattern = r"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
    match = re.match(pattern, string)
    return match is not None

def is_nanoid(string):
    pattern = r"^[A-Za-z0-9_-]{21}$"
    match = re.match(pattern, string)
    return match is not None

def is_ulid(string):
    pattern = r'^[A-Za-z0-9]{26}$'
    match = re.match(pattern, string)
    return match is not None