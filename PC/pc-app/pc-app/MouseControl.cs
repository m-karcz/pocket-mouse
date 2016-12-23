using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace pc_app
{
    class MouseControl
    {
        [DllImport("User32.Dll")]
        public static extern long SetCursorPos(int x, int y);

        private static ButtonState leftButton = ButtonState.Free;
        private static ButtonState rightButton = ButtonState.Free;

        [DllImport("user32.dll", CharSet = CharSet.Auto, CallingConvention = CallingConvention.StdCall)]
        private static extern void mouse_event(uint dwFlags, uint dx, uint dy, uint cButtons, uint dwExtraInfo);

        private const uint MOUSEEVENTF_LEFTDOWN = 0x02;
        private const uint MOUSEEVENTF_LEFTUP = 0x04;
        private const int MOUSEEVENTF_RIGHTDOWN = 0x08;
        private const int MOUSEEVENTF_RIGHTUP = 0x10;
        private const int MOUSEEVENTF_ABSOLUTE = 0x08000;

        public static void LeftClickAction(bool state)
        {

            if (state)
            {
                if (ButtonState.Free==leftButton)
                {
                    mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0);
                    leftButton = ButtonState.Hold;
                }
            }
            else
            {
                if (ButtonState.Hold == leftButton)
                {
                    mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
                    leftButton = ButtonState.Free;
                }

            }
        }
    }
    enum ButtonState
    {
        Hold,
        Free,

    }
}
