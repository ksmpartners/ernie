/*
	Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package com.ksmpartners.ernie.api

case class MissingArgumentException(msg: String) extends Exception(msg)
case class InvalidDefinitionException(msg: String) extends Exception(msg)
case class NotFoundException(msg: String) extends Exception(msg)
case class NothingToReturnException(msg: String) extends Exception(msg)
case class ReportOutputException(status: Option[com.ksmpartners.ernie.model.JobStatus], msg: String) extends Exception(msg) {
  def compare(e: Exception): Boolean = {
    if (e.isInstanceOf[ReportOutputException]) {
      if (status.isEmpty || e.asInstanceOf[ReportOutputException].status.isEmpty) true
      else e.asInstanceOf[ReportOutputException].status.equals(status)
    } else false
  }
}

