/*
 * Copyright (C) 2011-2015 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aestasit.infrastructure.winrm.client.request

import com.aestasit.infrastructure.winrm.client.util.Utils
import groovy.xml.MarkupBuilder

/**
 * Request to free resources after command execution.
 *
 * @author Sergey Korenko
 */
class CleanupCommandRequest extends WinRMRequest {

  String commandId
  String shellId

  CleanupCommandRequest(URL toAddress, String shellId, String commandId, int timeout = 60) {
    super(toAddress, timeout)
    this.shellId = shellId
    this.commandId = commandId
  }

  @Override
  String toString() {

    StringWriter writer = new StringWriter()
    MarkupBuilder xml = new MarkupBuilder(writer)

    xml.'s:Envelope'('xmlns:s': NMSP_URI_S,
        'xmlns:wsa': NMSP_URI_WSA,
        'xmlns:wsman': NMSP_URI_WSMAN) {
      's:Header' {
        'wsa:To'(toAddress)
        'wsman:ResourceURI'('s:mustUnderstand': true, URI_SHELL_CMD)
        'wsa:ReplyTo' {
          'wsa:Address'('s:mustUnderstand': true, URI_ADDRESS)
        }
        'wsa:Action'('s:mustUnderstand': true, 'http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Signal')
        'wsman:MaxEnvelopeSize'('s:mustUnderstand': true, envelopSize)
        'wsa:MessageID'(Utils.composeUUID())
        'wsman:Locale'('s:mustUnderstand': false, 'xml:lang': locale)
        'wsman:SelectorSet' {
          'wsman:Selector'(Name: 'ShellId', "${shellId}")
        }
        'wsman:OperationTimeout'(timeout)
      }
      's:Body' {
        'rsp:Signal'('xmlns:rsp': NMSP_URI_RSP, 'CommandId': commandId) {
          'rsp:Code'('http://schemas.microsoft.com/wbem/wsman/1/windows/shell/signal/terminate')
        }
      }
    }

    writer.toString()

  }

}
