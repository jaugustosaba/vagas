using System.Collections.Generic;
using Microsoft.AspNetCore.WebUtilities;

namespace Vagas.Controller
{
    public class JsonResult<T> {
        public int Status {get; set;}
        public string Message {get; set;}
        public T Result {get; set;}
        public List<string> Errors;

        public JsonResult(int status, T result) {
            this.Status = status;
            this.Message = ReasonPhrases.GetReasonPhrase(status);
            this.Result = result;
            this.Errors = null;
        }

        public JsonResult(int status, List<string> errors) {
            this.Status = status;
            this.Message = ReasonPhrases.GetReasonPhrase(status);
            this.Result = default(T);
            this.Errors = errors;
        }
    }

}