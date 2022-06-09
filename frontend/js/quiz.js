var quizID = undefined;
var questionsNo = undefined;

function startQuiz() {
  questionsNo = document.getElementById("quiz-mode-question-no").value;
  postQuizStart(questionsNo, (err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      console.log(data);
      quizID = data;
    }
  });
}
