# USB Midi Android Plugin

USB Midi Android Plugin is a plugin for communicate with a Midi device through USB.

## Usage

### Java Android


In the first place you have to inherits from `IMidiCallback` interface.

```java
public class MyCallback implements IMidiCallback {
    TextView _textView = null;
    public MyCallback(TextView textView)
    {
        _textView = textView;
    }
    @Override
    public void NoteOn(int note, int velocity) {
        _textView.append(" Note : " + note + " velocity : " + velocity + System.lineSeparator());
    }

    @Override
    public void NoteOff(int note) {
        _textView.append(" Note : " + note + System.lineSeparator());
    }

    @Override
    public void DeviceAttached(String name) {
        _textView.append(" DeviceAttached Name : " + name + System.lineSeparator());
    }

    @Override
    public void DeviceDetached(String name) {
        _textView.append(" DeviceDetached Name : " + name + System.lineSeparator());
    }
}
```

Then you have to call the method called `ctor` :

```java
public class MainActivity extends AppCompatActivity {

    private MyCallback _myCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _myCallback = new MyCallback(findViewById(R.id.textViewMidiLog));
        UsbMidiController.getInstance().ctor(_myCallback, this);
    }
}
```
Now you should be able to receive Midi events.
You can find the full code [here](./Examples/ExampleAndroidApplication).

### Unity Android

Create a Csharp script `MidiEventHandler` that inherits from `MonoBehaviour` and `IMidiEventHandler`.
In the awake and start methods add these lines for instantiating the plugin : 
```csharp

private void Awake()
{
    gameObject.AddComponent<MidiManager>();
}

private void Start()
{
    MidiManager.Instance.RegisterEventHandler(this);
}

```
Then implements all the methods of `IMidiEventHandler` :

```csharp
[SerializeField] private Text text;
// Called when you plug a midi note is down
public void NoteOn(int note, int velocity)
{
    Debug.Log("Note On " + note + " velocity " + velocity);
    text.text += "Note On " + note + " velocity " + velocity + Environment.NewLine;
}

// Called when you plug a midi note is released
public void NoteOff(int note)
{
    Debug.Log("Note off " + note);
    text.text += "Note off " + note + Environment.NewLine;
}

// Called when you plug a midi device
public void DeviceAttached(string deviceName)
{
    Debug.Log("Device Attached " + deviceName);
    text.text += "Device Attached " + deviceName + Environment.NewLine;
}
// Called when you unplug a midi device
public void DeviceDetached(string deviceName)
{
    Debug.Log("Device Detached " + deviceName);
    text.text += "Device Detached " + deviceName + Environment.NewLine;
}
```

In the editor, add a Text and add the previous script into your scene.
## License

[MIT](https://choosealicense.com/licenses/mit/)
