# Copyright (c) 2023 FRC 5990 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
# associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation
# the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
# persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this
# permission notice shall be included in all copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS",
# WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
# CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
# WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# Lets go Trigon

# if this library is not installed, dont install ntcore but pyntcore
import ntcore
import sys
import keyboard
import time

TEAM_NUMBER = 4590  # GREENBLITZ 🐐🐐🐐🐐
CLIENT_NAME = "KeyboardToNetworkTables"

CONNECTION_TIMEOUT_SECONDS = 60
CONNECTION_COOLDOWN_SECONDS = 0.1
KEYBOARD_CHECKING_COOLDOWN_SECONDS = 0.01

KEYBOARD_TABLE = "Keyboard"
KEYBOARD_KEYS_TABLE = "Keyboard/Keys"
IP = sys.argv[1]


def is_pressed(event: keyboard.KeyboardEvent):
    return event.event_type == keyboard.KEY_DOWN


def on_key_event(event: keyboard.KeyboardEvent, table: ntcore.NetworkTable):
    if event is None or event.name is None:
        return
    elif event.name == "/":
        table.putBoolean("slash", is_pressed(event))
    elif event.is_keypad:
        table.putBoolean("numpad" + event.name, is_pressed(event))
    else:
        table.putBoolean(event.name.lower(), is_pressed(event))


def get_network_table():
    network_table_instance = ntcore.NetworkTableInstance.getDefault()

    print("Setting up NetworkTables client for team {}".format(TEAM_NUMBER))
    network_table_instance.startClient4(CLIENT_NAME)
    network_table_instance.setServer(IP)
    network_table_instance.startDSClient()

    print("Waiting for connection to NetworkTables server...")
    starting_time = time.time()
    while not network_table_instance.isConnected():
        # terminate client and program if it takes too long to connect
        if time.time() - starting_time > CONNECTION_TIMEOUT_SECONDS:
            close_client(network_table_instance)
            sys.exit()
        time.sleep(CONNECTION_COOLDOWN_SECONDS)
    print("Connection to NetworkTables server succeeded!")

    return network_table_instance


def close_client(network_table_instance: ntcore.NetworkTableInstance):
    network_table_instance.stopDSClient()
    network_table_instance.stopClient()


def start_keyboard_tracking():
    network_table_instance = get_network_table()
    table = network_table_instance.getTable(KEYBOARD_KEYS_TABLE)

    keyboard.hook(lambda key_event: on_key_event(key_event, table))
    while network_table_instance.isConnected():
        time.sleep(KEYBOARD_CHECKING_COOLDOWN_SECONDS)
    close_client(network_table_instance)


if __name__ == "__main__":
    start_keyboard_tracking()