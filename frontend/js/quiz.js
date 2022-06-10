var quizID = undefined;
var totalQuestionsNo = undefined;
var currentQuestionNo = undefined;

function startQuiz() {
  totalQuestionsNo = document.getElementById("quiz-mode-question-no").value;
  postQuizStart(totalQuestionsNo, (err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      console.log(data);
      quizID = data;
    }
  });
}