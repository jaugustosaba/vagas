using System.Collections.Generic;
using System.Linq;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Http;
using Vagas.Models;
using System;

namespace Vagas.Controller
{

    [ApiController]
    [Route("/v1")]
    public  class VagasController: ControllerBase {

        static JsonResult<T> guard<T>(Func<T> action) {
            // try {
                return new JsonResult<T>(StatusCodes.Status200OK, action.Invoke());
            // } catch (Exception e) {
            //     return new JsonResult<T>(StatusCodes.Status500InternalServerError, new List<string>{e.Message});
            // }
        }

        static Level GetLevel(VagasContext context, long id) {
            Level level = context.Levels.FirstOrDefault((level) => level.Id == id);
            if (level == null) {
                throw new Exception($"Level `${id}` not found");
            }
            return level;
        }

        static Location GetLocation(VagasContext context, string name) {
            Location location = context.Locations.FirstOrDefault((location) => location.Name == name);
            if (location == null) {
                throw new Exception($"Location `${name}` not found");
            }
            return location;
        }

        static bool ApplicationExists(VagasContext context, long jobId, long applicantId) {
            var application = context.Applications.FirstOrDefault((app) => (app.JobId == jobId) && (app.ApplicantId == applicantId));
            return application != null;
        }

        static Job GetJob(VagasContext context, long jobId) {
            Job job = context.Jobs.FirstOrDefault(job => job.Id == jobId);
            if (job == null) {
                throw new Exception($"Cannot find job with id={jobId}");
            }
            return job;
        }

        static List<Tuple<long, int>> Neighbors(VagasContext context, long id) {
            return context.Distances
                .Where((d) => (d.FromLocationId == id) || (d.ToLocationId == id))
                .Select((d) => Tuple.Create((d.FromLocationId == id) ? d.ToLocationId : d.ToLocationId, d.Value))
                .ToList();
        }

        static int Distance(VagasContext context, long src, long dest) {
            var distances = new Dictionary<long, int>() {
                [src] = 0,
            };
            int dist(long id) 
                => distances.GetValueOrDefault(id, int.MaxValue);

            var frontier = new HashSet<long>() { src };
            var visited = new HashSet<long>();
            while (frontier.Count() > 0) {
                var near = distances
                    .Where(t => !visited.Contains(t.Key))
                    .OrderBy(t => t.Value)
                    .First();
                    
                var id = near.Key;
                var d = near.Value;

                visited.Add(id);
                frontier.Remove(id);
                if (id == dest)
                    break;
                
                foreach (var neighbor in Neighbors(context, id)) {
                    var nid = neighbor.Item1;
                    if (visited.Contains(nid)) {
                        continue;
                    }
                    var nd = d + neighbor.Item2;
                    if (nd < dist(nid)) {
                        distances[nid] = nd;
                        frontier.Add(nid);
                    }
                }
            }
            return dist(dest);
        }

        static long Score(VagasContext context, Job job, Applicant applicant) {
            var D = Distance(context, job.Id, applicant.Id) switch
            {
                int x when (x < 5) => 100,
                int x when (x < 10) => 75,
                int x when (x < 15) => 50,
                int x when (x < 20) => 25,
                _ => 0
            };
            var N = 100 - 25 * Math.Abs(job.LevelId - applicant.LevelId);
            return (N + D) / 2;
        }

        static List<Tuple<Applicant, long>> RankEx(VagasContext context, long jobId) {
            return context.Applications
                .Where((app) => app.JobId == jobId)
                .Select((app) => Tuple.Create(app.Applicant, Score(context, app.Job, app.Applicant)))
                .OrderByDescending((tuple) => tuple.Item2)
                .ToList();
        }

        [HttpPost("vagas")]
        public JsonResult<long> AddJob([FromServices] VagasContext context, [FromBody] JobJson jobJson) {
            return guard(() => {
                var job = new Job {
                    Company = jobJson.Company,
                    Title = jobJson.Title,
                    Description = jobJson.Description,
                    LevelId = GetLevel(context, jobJson.Level).Id,
                    LocationId = GetLocation(context, jobJson.Location).Id,
                };
                context.Add(job);
                context.SaveChanges();
                return job.Id;
            });
        }

        [HttpPost("pessoas")]
        public JsonResult<long> AddApplicant([FromServices] VagasContext context, [FromBody] ApplicantJson applicantJson) {
            return guard(() => {
                var applicant = new Applicant {
                    Name = applicantJson.Name,
                    Profession = applicantJson.Location,
                    LevelId = GetLevel(context, applicantJson.Level).Id,
                    LocationId = GetLocation(context, applicantJson.Location).Id,
                };
                context.Applicants.Add(applicant);
                context.SaveChanges();
                return applicant.Id;
            });
        }

        [HttpPost("candidaturas")]
        public JsonResult<bool> ApplyToJob([FromServices] VagasContext context, [FromBody] ApplicationJson applicationJson) {
            return guard(() => {
                if (ApplicationExists(context, applicationJson.JobId, applicationJson.ApplicantId)) {
                    return false;
                }
                var application = new Application {
                    ApplicantId = applicationJson.ApplicantId,
                    JobId = applicationJson.JobId,
                };
                context.Applications.Add(application);
                context.SaveChanges();
                return true;
            });
        }

        [HttpGet("vagas/{id}/candidaturas/ranking")]
        public JsonResult<List<ScoreJson>> Rank([FromServices] VagasContext context, [FromRoute] long jobId) {
            return guard(() => {
                var ranking = RankEx(context, jobId);
                return ranking.Select((tuple) => new ScoreJson() {
                    Name = tuple.Item1.Name,
                    Profession = tuple.Item1.Profession,
                    Localization = tuple.Item1.Location.Name,
                    Level = tuple.Item1.Level.Id,
                    score = tuple.Item2,
                }).ToList();
            });
        }
    }

}