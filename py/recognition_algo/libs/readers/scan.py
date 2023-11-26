from typing import Callable

def scan_dict(data: dict, scan_fc: Callable[str, bool]) -> list:

    def check_for_bus(data, path, buses):
        if isinstance(data, dict):
            for key, value in data.items():
                if isinstance(value, str) and scan_fc(value):
                    buses.append(path + "." + key)
                check_for_bus(value, path + "." + key, buses)

    buses = []
    check_for_bus(data, "", buses)
    return buses